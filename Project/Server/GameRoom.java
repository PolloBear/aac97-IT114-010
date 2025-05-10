package Project.Server;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.PointsPayload;
import Project.Common.ReadyPayload;
import Project.Common.TimedEvent;
import Project.Common.User;
import Project.Exceptions.NotReadyException;
import Project.Exceptions.PhaseMismatchException;
import Project.Exceptions.PlayerNotFoundException;
import Project.Common.PayloadType;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;

    private int round = 0;
    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerThread sp) {
        // sync GameRoom state to new client
        syncCurrentPhase(sp);
        syncReadyStatus(sp);
        syncTurnStatus(sp);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerThread sp) {
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + clientsInRoom.size());
        if (clientsInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        }
    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Round Time: " + time));
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> System.out.println("Turn Time: " + time));
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        round = 0;
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
protected void onRoundStart() {
    LoggerUtil.INSTANCE.info("onRoundStart() start");
    resetRoundTimer();
    resetTurnStatus();
    round++;
    relay(null, String.format("Round %d has started", round));

    
    startRoundTimer();
    LoggerUtil.INSTANCE.info("onRoundStart() end");
}


    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring

        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }


////// shows who the winner is 
    private void broadcastWinner() {
        User winner = clientsInRoom.values().stream()
            .map(ServerThread::getUser)
            .filter(u -> !u.isEliminated())
            .findFirst()
            .orElse(null);
    
        if (winner != null) {
            relay(null, "Game Over! The winner is: " + winner.getDisplayName());
        }
    }
    
    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
protected void onRoundEnd() {
    LoggerUtil.INSTANCE.info("onRoundEnd() start");
    resetRoundTimer(); // reset timer if round ended without the time expiring


    processBattles();
    syncPointsToClients();

    // Check for winner by points instead of eliminations
    final int WINNING_SCORE = 3; // or whatever number you want
    User winner = clientsInRoom.values().stream()
        .map(ServerThread::getUser)
        .filter(u -> u.getPoints() >= WINNING_SCORE)
        .findFirst()
        .orElse(null);

    if (winner != null) {
        relay(null, "Game Over! The winner is: " + winner.getDisplayName());
        onSessionEnd();
        return;
    }

    // Reset eliminated status (so players can continue next round)
    clientsInRoom.values().forEach(sp -> {
        User user = sp.getUser();
        if (!user.isEliminated()) {
            user.resetChoice(); // only reset choice for players who are still in the game
        }
    });

    onRoundStart(); // Continue to next round
    LoggerUtil.INSTANCE.info("onRoundEnd() end");
}



    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        resetReadyStatus();
        resetTurnStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerUser(s)
    private void sendResetTurnStatus() {
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendResetTurnStatus();
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    private void sendTurnStatus(ServerThread client, boolean tookTurn) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(client.getClientId(), client.didTakeTurn());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void syncTurnStatus(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendTurnStatus(serverUser.getClientId(),
                        serverUser.didTakeTurn(), true);
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    // end send data to ServerThread(s)

    // misc methods
    private void resetTurnStatus() {
        clientsInRoom.values().forEach(sp -> {
            sp.setTookTurn(false);
        });
        sendResetTurnStatus();
    }

    private void checkAllTookTurn() {
        int numReady = (int) clientsInRoom.values().stream()
                .filter(ServerThread::isReady)
                .count();
        int numTookTurn = (int) clientsInRoom.values().stream()
                .filter(sp -> sp.isReady() && sp.didTakeTurn())
                .count();
        if (numReady == numTookTurn) {
            relay(null, String.format("All players have taken their turn (%d/%d) ending the round", numTookTurn, numReady));
            onRoundEnd();
        }
    }

    // receive data from ServerThread (GameRoom specific)

    /**
     * Example turn action
     * 
     * @param currentUser
     */
    protected void handleTurnAction(ServerThread currentUser, String exampleText) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkIsReady(currentUser);
            if (currentUser.didTakeTurn()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already taken your turn this round");
                return;
            }
            currentUser.setTookTurn(true);
            // TODO handle example text possibly or other turn related intention from client
            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            checkAllTookTurn();
        }
        catch(NotReadyException e){
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } 
        catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only take a turn during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        }
    }

    // end receive data from ServerThread (GameRoom specific)

   

    protected void handlePick(ServerThread sender, String choice) {
        try {
            checkPlayerInRoom(sender);
            checkCurrentPhase(sender, Phase.IN_PROGRESS);
            checkIsReady(sender);
            User player = sender.getUser();
    
            if (player.isEliminated()) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "You are eliminated and cannot pick.");
                return;
            }
    
            if (player.getChoice() != null) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already made your choice.");
                return;
            }
    
            if (!choice.matches("[rps]")) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "Invalid choice. Use /pick r, /pick p, or /pick s.");
                return;
            }
    
            player.setChoice(choice);
            sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "Your choice has been recorded.");
    
            checkAllPicked();
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handlePick exception", e);
        }
    }
    

    private void checkAllPicked() {
        long activePlayers = clientsInRoom.values().stream()
            .map(ServerThread::getUser)
            .filter(u -> !u.isEliminated())
            .count();
    
        if (activePlayers <= 1) {
            // Not enough active players to trigger a full round
            return;
        }
    
        boolean allPicked = clientsInRoom.values().stream()
            .map(ServerThread::getUser)
            .filter(u -> !u.isEliminated())
            .allMatch(u -> u.getChoice() != null);
    
        if (allPicked) {
            onRoundEnd(); // Only trigger round end if >1 players are active AND all have picked
        }
    }
    
//
private void processBattles() {
    List<User> activeUsers = clientsInRoom.values().stream()
        .map(ServerThread::getUser)
        .filter(u -> !u.isEliminated() && u.getChoice() != null)
        .toList();

    if (activeUsers.size() < 2) {
        return;
    }

    // Count how many chose each option
    long rockCount = activeUsers.stream().filter(u -> u.getChoice().equals("r")).count();
    long paperCount = activeUsers.stream().filter(u -> u.getChoice().equals("p")).count();
    long scissorsCount = activeUsers.stream().filter(u -> u.getChoice().equals("s")).count();

    // Determine what beats what
    String winningChoice = null;

    if (rockCount > 0 && paperCount > 0 && scissorsCount == 0) {
        winningChoice = "p"; // Paper beats Rock
    } else if (rockCount > 0 && paperCount == 0 && scissorsCount > 0) {
        winningChoice = "r"; // Rock beats Scissors
    } else if (rockCount == 0 && paperCount > 0 && scissorsCount > 0) {
        winningChoice = "s"; // Scissors beats Paper
    }

    for (ServerThread sp : clientsInRoom.values()) {
        User user = sp.getUser();

        if (user.isEliminated() || user.getChoice() == null) continue;

        if (winningChoice == null) {
            sp.sendMessage("It's a tie with everyone!");
        } else if (user.getChoice().equals(winningChoice)) {
            user.addPoint();
            sp.sendMessage("You won this round!");
        } else {
            user.eliminate();
            sp.sendMessage("You lost this round.");
        }
    }
}


//
private String evaluateBattle(String a, String b) {
    if (a.equals(b)) return "tie";
    if ((a.equals("r") && b.equals("s")) ||
        (a.equals("p") && b.equals("r")) ||
        (a.equals("s") && b.equals("p"))) {
        return "a";
    } else {
        return "b";
    }
}
private void syncPointsToClients() {
    PointsPayload pp = new PointsPayload();
    Map<Long, Integer> pointsMap = clientsInRoom.values().stream()
        .collect(Collectors.toMap(
            ServerThread::getClientId,
            sp -> sp.getUser().getPoints()
        ));
    pp.setPointsMap(pointsMap);

    clientsInRoom.values().forEach(sp -> sp.sendToClient(pp));
}
protected void changePhase(Phase newPhase) {
    this.currentPhase = newPhase;
    clientsInRoom.values().forEach(sp -> sp.sendCurrentPhase(newPhase));
}

}