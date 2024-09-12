package bguspl.set.ex;

import bguspl.set.Env;


import java.util.Random;
import java.util.Vector;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    Vector<Integer> queue;


    Dealer dealer;
    volatile boolean canplay;

    boolean canPress;
    Thread point_timer_thread;
    Thread penalty_timer_thread;


    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.queue = new Vector<>(3);
        this.terminate = false;
        this.dealer = dealer;
        this.canplay = true;
        canPress = true;


    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {

        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");

        if (!human)
            createArtificialIntelligence();

        while (!terminate) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (queue.size() == 3 & canplay) {
                canPress = false;
                dealer.check_player_set(queue, id);
            }
        }
        if (!human) try {
            aiThread.join();
        } catch (InterruptedException ignored) {
        }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");

            int rows = env.config.rows;
            int columns = env.config.columns;
            while (!terminate) {
                //// this is the runnable method for the thread
                if (dealer.displayCounter != 0) {
                    try {
                        aiThread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Random random = new Random();
                    int randomRow = random.nextInt(rows); // Random row index from 0 to rows-1
                    int randomColumn = random.nextInt(columns); // Random column index from 0 to columns-1
                    int slot = randomColumn + columns * randomRow;
                    slot++;
                    keyPressed(-slot);
                }
            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();

    }


    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        wakeUpPlayer();

        if (point_timer_thread != null) {
            try {
                point_timer_thread.join();
            } catch (InterruptedException e) {
            }
        }

        if (penalty_timer_thread != null) {
            try {
                penalty_timer_thread.join();
            } catch (InterruptedException e) {
            }
        }

        if (aiThread != null) {
            try {
                aiThread.join();
            } catch (InterruptedException e) {
            }
        }

        try {
            playerThread.interrupt();
            playerThread.join();
        } catch (InterruptedException e) {
        }
    }


    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        if (!human && slot >= 0 || dealer.displayCounter == -100000 ||dealer.displayCounter==0)
            return;
        else if (!human)
            slot = -(slot + 1);


        if (canPress && table.slotToCard[slot] != null) {
            if (queue.contains(slot)) {
                queue.remove((Object) slot);
                env.ui.removeToken(id, slot);
                canplay = true;

            } else {
                if (queue.size() < 3) {
                    queue.add(slot);// add token
                    table.placeToken(id, slot);
                    if (queue.size() == 3)
                        wakeUpPlayer();
                }
            }
        }

    }

    public synchronized void wakeUpPlayer() {
        notify();
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        //   env.ui.setScore(id, score++);
        score = score + 1;
        env.ui.setScore(id, score);
        queue = new Vector<>(3);   // empty the queue
        point_timer_thread = new Thread(() -> {
            point_timer(); // Call the method inside the lambda expression
        });

        point_timer_thread.start();

        try {
            if (aiThread != null) aiThread.sleep(env.config.pointFreezeMillis);
           // if (playerThread != null)
             //   playerThread.interrupt();
            else
            playerThread.sleep(env.config.pointFreezeMillis);
        } catch (InterruptedException e) {
            //  throw new RuntimeException(e);
        }
        canPress = true;
    }


    /**
     * Penalize a player and perform other related actions.
     */

    public void penalty() {
        if (score > 0) {
            score--;
            env.ui.setScore(id, score);
        }
        penalty_timer_thread = new Thread(() -> {
            penalty_timer(); // Call the method inside the lambda expression
        });

        penalty_timer_thread.start();

        try {
            if (aiThread != null) aiThread.sleep(env.config.penaltyFreezeMillis);
            else playerThread.sleep(env.config.penaltyFreezeMillis);
        } catch (InterruptedException e) {
            //   throw new RuntimeException(e);
        }
        if (queue.size()==3)
        canplay = false;

        canPress = true;

    }



    public int score() {
        return score;
    }

    // we add this :
    void point_timer() {
        for (int i = 0; !terminate && i < env.config.pointFreezeMillis / 100; i++) {
            env.ui.setFreeze(id, env.config.pointFreezeMillis - 100L * i + 950);
            try {
                Thread.sleep(97);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        env.ui.setFreeze(id, 0);
    }

    void penalty_timer() {
        for (int i = 0; !terminate && i < env.config.penaltyFreezeMillis / 100; i++) {
            env.ui.setFreeze(id, env.config.penaltyFreezeMillis - 100L * i + 950);
            try {
                Thread.sleep(97);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        env.ui.setFreeze(id, 0);


    }


}
