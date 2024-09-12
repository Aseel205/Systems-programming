package bguspl.set.ex;

import bguspl.set.Env;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime;
    int[] card_slots;
    int[] cards;
    Thread timer_display_thread;
    volatile long displayCounter;

    final Object lock1;
    volatile boolean game_over;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        card_slots = give_me_array();
        cards = give_me_array();
        terminate = false;
        this.timer_display_thread = new Thread(() -> {
            updateTimerDisplay(); // Call the method inside the lambda expression
        });
        displayCounter = -100000;
        lock1 = new Object();
        game_over = false;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {

        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");

        for (Player p : players) {
            Thread PlayerThread = new Thread(p);
            PlayerThread.start();
        }

        //  start the timer
        while (!shouldFinish()) {
            placeCardsOnTable();
            if (displayCounter == -100000)
                timer_display_thread.start();
            timerLoop();
            removeAllCardsFromTable();

        }
        announceWinners();
        game_over = true;

        if (!terminate)
            terminate();

    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {

        while (!terminate) {
            displayCounter = env.config.turnTimeoutMillis + 100;
            sleepUntilWokenOrTimeout();   // wait for a countdown or player set claim
            if ((displayCounter == 0 && cards[0] == -1) || (terminate))
                return;
            removeCardsFromTable();
            placeCardsOnTable();

        }
    }


    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        for (Player player : players) {
            player.terminate();

        }
        try {
            timer_display_thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        synchronized (lock1) {
            if (!game_over)
                lock1.notifyAll();
        }
        if (!game_over)
            env.ui.dispose();

    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).isEmpty();
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private  void removeCardsFromTable() {  // to do

        for (int i : card_slots) {
            table.removeCard(i);
            for (Player p : players) {
               // if (p.queue.contains((Object) i))
                    p.queue.remove((Object) i);  //  here we have a problem

                //p.canplay=true;

            }
        }
        cards = give_me_array();
        card_slots = give_me_array();

    }


    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() { // to do
        Vector new_deck = new Vector<>();
        Collections.shuffle(deck);
        Iterator<Integer> iter = deck.iterator();
        int slot = 0;
        for (int i = 0; i < env.config.columns; i++)
            for (int j = 0; j < env.config.rows; j++) {
                slot = i + env.config.columns * j;
                if (table.slotToCard[slot] == null)
                    if (iter.hasNext()) {
                        int iter_value = iter.next();
                        table.placeCard(iter_value, slot);
                    }
            }
        while (iter.hasNext())
            new_deck.add(iter.next());

        deck = new_deck;
        synchronized (lock1) {
            lock1.notify();
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        synchronized (lock1) {
            try {
                // Wait until notified
                lock1.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupted status
            }
        }
    }


    private synchronized void wakeUp() {
        synchronized (lock1) {
            if (displayCounter != 0)
                return;
            lock1.notify();
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay() {
        boolean warn;
        if (displayCounter == env.config.turnTimeoutMillis + 100)
            while (!terminate) {


                if (this.displayCounter > 0)
                    displayCounter = displayCounter - 100;

                warn = this.displayCounter <= env.config.turnTimeoutWarningMillis;

                if (! warn)
                    env.ui.setCountdown(displayCounter + 950, warn);
                else
                    env.ui.setCountdown(displayCounter, warn);

                if (this.displayCounter == 0) {
                    wakeUp();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            }


    }

    private int[] give_me_array() {
        int[] output = new int[3];
        output[0] = -1;
        output[1] = -1;  // I can remove this line
        output[2] = -1;  // I can remove this line
        return output;



    }


    /**
     * Returns all the cards from the table to the deck.
     */
    private synchronized void removeAllCardsFromTable() {
        Integer[] slotsCard = table.slotToCard;
        for (int i = 0; i < slotsCard.length; i++) {
            if (slotsCard[i] != null) {
                deck.add(slotsCard[i]);
                table.removeCard(i);
            }
        }
        for (Player p : players) {
            p.queue = new Vector<>();
            p.canplay=true;
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        int winnersScore = 0;
        int winnersNumber = 0;
        for (Player player : players)
            if (winnersScore < player.score())
                winnersScore = player.score();

        for (Player player : players)
            if (player.score() == winnersScore)
                winnersNumber++;
        int[] winnerPlayers = new int[winnersNumber];

        int i = 0;
        for (Player player : players)
            if (player.score() == winnersScore) {
                winnerPlayers[i] = player.id;
                i++;
            }
        env.ui.announceWinner(winnerPlayers);


    }

    // our methods
    public void check_player_set(Vector<Integer> input, int id) {
        boolean ans;
        synchronized (this) {
            if (players[id].queue.size() != 3) {    //  & displayCounter==0
                players[id].canPress = true;
                return;
            }
            cards = give_me_array();
            card_slots = give_me_array();
            int iter_value;
            Iterator<Integer> iter = input.iterator();
            int index = 0;
            while (iter.hasNext()) {
                iter_value = iter.next();
                card_slots[index] = iter_value;
                cards[index] = table.slotToCard[card_slots[index]];
                index++;
            }
            synchronized (lock1) {
                ans = env.util.testSet(cards);
                if (ans) {
                    try {
                        lock1.notify();
                        lock1.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                card_slots = give_me_array();
                cards = give_me_array();
            }
        }
        if (ans) {
            players[id].point();
        } else {
            players[id].penalty();

        }


    }
}