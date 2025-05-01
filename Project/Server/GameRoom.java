package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;
import Project.Common.TimerType;
import Project.Exceptions.MissingCurrentPlayerException;
import Project.Exceptions.NotPlayersTurnException;
import Project.Exceptions.NotReadyException;
import Project.Exceptions.PhaseMismatchException;
import Project.Exceptions.PlayerNotFoundException;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private List<ServerThread> turnOrder = new ArrayList<>();
    private long currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
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
        syncPlayerPoints(sp);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerThread sp) {
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + clientsInRoom.size());
        long removedClient = sp.getClientId();
        turnOrder.removeIf(player -> player.getClientId() == sp.getClientId());
        if (clientsInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        } else if (removedClient == currentTurnClientId) {
            onTurnStart();
        }
    }

    // timer handlers
    @SuppressWarnings("unused")
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> {
            System.out.println("Round Time: " + time);
            sendCurrentTime(TimerType.ROUND, time);
        });
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
            sendCurrentTime(TimerType.ROUND, -1);
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> {
            System.out.println("Turn Time: " + time);
            sendCurrentTime(TimerType.TURN, time);
        });
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
            sendCurrentTime(TimerType.TURN, -1);
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        setTurnOrder();
        round = 0;

        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        round++;
        // relay(null, String.format("Round %d has started", round));
        sendGameEvent("Round: " + round);
        resetRoundTimer();
        resetTurnStatus();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();

        for (ServerThread player : clientsInRoom.values()) {
            player.setChoice(null);
            player.setTookTurn(false);
        }
        
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        ServerThread currentPlayer = null;
        try {
            currentPlayer = getNextPlayer();
            relay(null, String.format("It's %s's turn", currentPlayer.getDisplayName()));
        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
        }

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
        try {
            ServerThread currentPlayer = getCurrentPlayer();
            if (currentPlayer.getPoints() >= 3) {
                relay(null, String.format("%s has won the game!", currentPlayer.getDisplayName()));
                LoggerUtil.INSTANCE.info("onTurnEnd() end"); // added here for consistent lifecycle logs
                onSessionEnd();
                return;
            }
            // optionally can use checkAllTookTurn();
            if (isLastPlayer()) {
                // if the current player is the last player in the turn order, end the round
                onRoundEnd();
            } else {
                onTurnStart();
            }
        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
        }
        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer();

        List<ServerThread> players = new ArrayList<>(clientsInRoom.values());
        if (players.size() < 2) {
            sendGameEvent("Not enough players to evaluate a round.");
            onRoundStart();
            return;
        }

     // Collect choices
        int rock = 0, paper = 0, scissors = 0;
        for (ServerThread p : players) {
            String c = p.getChoice();
            if (c == null) {
                p.setEliminated(true);
                sendGameEvent(p.getDisplayName() + " did not choose and is eliminated.");
                
                continue;
            }
            switch (c) {
                case "rock": rock++; break;
                case "paper": paper++; break;
                case "scissors": scissors++; break;
            }
        }
 
     // Determine winner(s)
        String winningChoice = null;
 
        if (rock > 0 && paper > 0 && scissors > 0) {
            sendGameEvent("It's a tie! All three choices were played.");
        } else if (rock > 0 && paper > 0 && scissors == 0) {
            winningChoice = "paper";
        } else if (rock > 0 && scissors > 0 && paper == 0) {
            winningChoice = "rock";
        } else if (paper > 0 && scissors > 0 && rock == 0) {
            winningChoice = "scissors";
        } else {
            sendGameEvent("It's a tie!");
        }
 
        if (winningChoice != null) {
            for (ServerThread p : players) {
                if (winningChoice.equals(p.getChoice())) {
                    p.changePoints(1);
                    sendPlayerPoints(p);
                    sendGameEvent(p.getDisplayName() + " wins the round!");
                }
            }
        }
  
        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        onRoundStart();
    }


    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        turnOrder.clear();
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        // reset any pending timers
        resetTurnTimer();
        resetRoundTimer();
        resetTurnStatus();
        resetReadyStatus();
        resetTurnStatus();
        clientsInRoom.values().stream().forEach(s -> s.setPoints(0));
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerUser(s)
    private void syncPlayerPoints(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendPlayerPoints(serverUser.getClientId(),
                        serverUser.getPoints());
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    private void sendPlayerPoints(ServerThread sp) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPlayerPoints(sp.getClientId(), sp.getPoints());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void sendGameEvent(String str) {
        sendGameEvent(str, null);
    }

    private void sendGameEvent(String str, List<Long> targets) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean canSend = false;
            if (targets != null) {
                if (targets.contains(spInRoom.getClientId())) {
                    canSend = true;
                }
            } else {
                canSend = true;
            }
            if (canSend) {
                boolean failedToSend = !spInRoom.sendGameEvent(str);
                if (failedToSend) {
                    removeClient(spInRoom);
                }
                return failedToSend;
            }
            return false;
        });
    }

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

    private void setTurnOrder() {
        turnOrder.clear();
        turnOrder = clientsInRoom.values().stream().filter(ServerThread::isReady).collect(Collectors.toList());
        Collections.shuffle(turnOrder);
    }

    private ServerThread getCurrentPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // quick early exit
        if (currentTurnClientId == Constants.DEFAULT_CLIENT_ID) {
            throw new MissingCurrentPlayerException("Current Plaer not set");
        }
        return turnOrder.stream()
                .filter(sp -> sp.getClientId() == currentTurnClientId)
                .findFirst()
                // this shouldn't occur but is included as a "just in case"
                .orElseThrow(() -> new PlayerNotFoundException("Current player not found in turn order"));
    }

    private ServerThread getNextPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        int index = 0;
    if (currentTurnClientId != Constants.DEFAULT_CLIENT_ID) {
        index = turnOrder.indexOf(getCurrentPlayer()) + 1;
    }
    int originalIndex = index;
    for (int i = 0; i < turnOrder.size(); i++) {
        ServerThread candidate = turnOrder.get(index % turnOrder.size());
        if (!candidate.isEliminated()) {
            currentTurnClientId = candidate.getClientId();
            return candidate;
        }
        index++;
    }

        throw new MissingCurrentPlayerException("No non-eliminated players found");
    }

        private boolean isLastPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // check if the current player is the last player in the turn order
        return turnOrder.indexOf(getCurrentPlayer()) == (turnOrder.size() - 1);
    }

    @SuppressWarnings("unused")
    private void checkAllTookTurn() {
        int numReady = clientsInRoom.values().stream()
                .filter(sp -> sp.isReady())
                .toList().size();
        int numTookTurn = clientsInRoom.values().stream()
                // ensure to verify the isReady part since it's against the original list
                .filter(sp -> sp.isReady() && sp.didTakeTurn())
                .toList().size();
        if (numReady == numTookTurn) {
            relay(null,
                    String.format("All players have taken their turn (%d/%d) ending the round", numTookTurn, numReady));
            onRoundEnd();
        }
    }

    // start check methods
    

    // end check methods

    /**
     * Example turn action
     * 
     * @param currentUser
     */
    protected void handleTurnAction(ServerThread currentUser, String choiceText) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            //checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
    
            if (currentUser.didTakeTurn()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already taken your turn this round");
                return;
            }
            if (currentUser.isEliminated()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have been eliminated and can no longer play.");
                return;
            }
            
            
            currentUser.setChoice(choiceText.toLowerCase());
            sendGameEvent(currentUser.getDisplayName() + " finished their turn");
    
            currentUser.setTookTurn(true);
            int readyPlayers = (int) clientsInRoom.values().stream()
                .filter(ServerThread::didTakeTurn)
                .count();

            int totalPlayers = clientsInRoom.size();

            if (readyPlayers == totalPlayers) {
            onRoundEnd(); 
            }

            sendTurnStatus(currentUser, true);
    
        } catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PlayerNotFoundException e) {
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
}