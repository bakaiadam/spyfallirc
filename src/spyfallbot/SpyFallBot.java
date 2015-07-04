/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spyfallbot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
//import jdk.net.SocketFlow;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 *
 * @author adam
 */
public class SpyFallBot extends PircBot {


    public SpyFallBot() throws IOException
    {
        locations=readLines("locations.txt");
    }
    static final String main_channel = "#tarsasjatek";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        // Now start our bot up.
        final SpyFallBot bot = new SpyFallBot();

        bot.setName("SpyFallBot");

//bot.setTopic(VERSION, VERSION);
        // Enable debugging output.
        bot.setVerbose(true);

        // Connect to the IRC server.
        bot.connect("irc.freenode.net");

        // Join the #pircbot channel.
        bot.joinChannel(main_channel);

        new Thread()
        {
            public void run() {
                //System.err.println("run thread");
                while (true)
                {
                  //  System.err.println("run thread2");
                try {
                    sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SpyFallBot.class.getName()).log(Level.SEVERE, null, ex);
                }
                User[] h=bot.getUsers(main_channel);
                if (h.length!=0)
                    bot.onUserList(main_channel, h);
        //bot.getUsers(main_channel);
                }
    }
        }.start();
    }

    String[] locations = null;

    TreeSet<String> users = new TreeSet<>();

    //enum State{};
    abstract class Phase {

        public void onPrivateMessage(String sender,
                String login,
                String hostname,
                String message) {
            if (message.equals("!list")) {
                String ret = new String("");
                for (String q : locations) {
                    ret = ret + q + ",";
                }
                sendMessage(sender, ret);
            }
        }
public void playersattemoment()
{
                    String ret = "Players at the moment:";
                    for (String g : users) {
                        ret = ret + g + ",";
                    }
                    SpyFallBot.this.sendMessage(main_channel, ret);
    
}
        public void onMessage(String channel, String sender,
                String login, String hostname, String message) {

            if (channel.equals(main_channel)) {
                if (message.equals("!join") || message.equals("!leave") || message.equals("!help") ) {
                    if (message.equals("!join")) {
                        users.add(sender);
                    } else if (message.equals("!leave")) {
                        users.remove(sender);
                    }
                    else if (message.equals("!help")) {
                        sendMessage(channel, "Hi! This is spyfall irc bot! The game has three phases:lobby,Talking,Voting. In lobby, you can join the game with !join, leave by !leave, and start the game with !start.After start command was executed,players get the location information and one of them the spy. In this phase, the spy can guess the location with the !guess <location> command.(The spy can check the list of locations by sending the private message !list to the bot), and players can start a vote for a suspicious other player with !vote <nickname>. When the voting started, everybody except the accused should write !yes to have a succesful vote. only one veto with !no can stop the voting.When spy or non spies win, the game goes back to the lobby.If the game goes buggy you can go back by the !lobby command, or you can go back and clear the player list by the !reset command.(This stuff made by Ádám Bakai. You can contact me on bakaiadam@gmail.com )");
                        sendMessage(channel, "Current phase:"+this.getClass().getSimpleName());
                    }
    playersattemoment();
                } else if (message.equals("!lobby")) {
                    p = new Lobby();
                }
                if (message.equals("!reset")) {
                    users.clear();
                    p = new Lobby();
                } else {
                    onMessage2(channel, sender, login, hostname, message);
                }
            }
        }

        public abstract void onMessage2(String channel, String sender,
                String login, String hostname, String message);

        private void onUserList(String channel, User[] users2) {

           // System.err.println("onuserlist");
            TreeSet<String> unow = new TreeSet<>();
            for (User u : users2) {
                unow.add(u.getNick());
            }
boolean removed=false;
            for (String s : new TreeSet<>(users) ) {
                if (unow.contains(s) == false) {
                    users.remove(s);
removed=true;
                    sendMessage(channel, s + " removed from user list");
                }
            }
            if (removed) playersattemoment();
            onUserList2(channel, unow);

        }

        private void onUserList2(String channel, TreeSet<String> unow) {
        }
    }

    class Lobby extends Phase {

        public Lobby() {
            sendMessage(main_channel, "Welcome to lobby! You can start the game with the !start command. You can join with !join, and leave with !leave. You can reset the whole game(in case of bug)with !reset command).Full help command: !help ");
            playersattemoment();
            
        }

        @Override
        public void onMessage2(String channel, String sender, String login, String hostname, String message) {
            //users.add(SpyFallBot.this.);
            if (message.equals("!start") && users.contains(sender)) {
                if (users.size() >= 3) {
                    p = new Talking();
                } else {
                    SpyFallBot.this.sendMessage(channel, "You need at least three players");
                }
            }

        }

    }

    class Talking extends Phase {

        List<String> playing_users = new ArrayList<>(users);
        Set<String> playing_users_set = new TreeSet<>(users);
        Set<String> already_nominated = new TreeSet<>();
        String spy = null;
        String location = null;

        public Talking() {
            int idx = new Random().nextInt(locations.length);
            location = locations[idx];

            Collections.shuffle(playing_users);
            spy = playing_users.get(0);
            Collections.shuffle(playing_users);
            for (String g : playing_users) {
                if (g.equals(spy)) {
                    SpyFallBot.this.sendMessage(g, "You are the spy! You can check the possible locations with the !list command.");
                } else {
                    SpyFallBot.this.sendMessage(g, "You are here:" + location);
                }

            }
            //send the stuff in private to everybody
            sendMessage(main_channel, "Everybody recieved a message with ther role.Let's start playing.");
        }

        @Override
        public void onMessage2(String channel, String sender, String login, String hostname, String message) {
            if (playing_users_set.contains(sender) == false) {
                return;
            }
            if (message.startsWith("!guess")) {
                if (message.indexOf(" ") == -1) {
                    sendMessage(channel, "I don't know which location is your guess!");
                } else {
                    if (sender.equals(spy)) {

                        String guess = message.substring(message.indexOf(" ") + 1);
                        if (guess.equals(location)) {
                            sendMessage(channel, "The spy guessed the location correctly. He won!");
                        } else {
                            sendMessage(channel, "The spy guessed the location incorrectly. Non Spies won!");
                        }
                    } else {
                        sendMessage(channel, "You were not the spy, you shouldn't have tried to guess!Now the game is over");
                    }
                    p = new Lobby();

                }
            } else if (message.startsWith("!vote")) {
                if (already_nominated.contains(sender)) {
                    sendMessage(channel, "You already tried to nominate, you can't do it again!");
                } else {
                    String name = message.substring(message.indexOf(" ") + 1);
                    if (name.equals(sender)) {
                        sendMessage(channel, "You can't nominate yourself as a spy!");
                    } else {
                        boolean good = playing_users_set.contains(name);
                        if (good) {
                            p = new Voting(this, sender, name);
                            already_nominated.add(sender);
                        } else {
                            String ret = "No such nick is playing,you options are:";
                            for (String o : playing_users_set) {
                                ret = ret + o + ",";
                            }
                            sendMessage(channel, ret);
                        }
                    }
                }
            }
        }

        private void onUserList2(String channel, TreeSet<String> unow) {
            for (String s : playing_users_set) {
                if (unow.contains(s) == false) {
                    playing_users_set.remove(s);
                    if (s.equals(spy)) {
                        sendMessage(channel, spy + "was the spy and quited. Back to the lobby!");
                        p = new Lobby();
                    } else {
                        if (playing_users_set.size() < 3) {
                            sendMessage(channel, "Somebody quited, and now there aren't enough players. Back to the lobby!");
                            p = new Lobby();
                        } else {
                            sendMessage(channel, s + " quited. But he wasn't the spy, so it's ok, the game can continue.");
                        }
                    }

                    //sendMessage(channel, "");
                }
            }
        }

    }

    class Voting extends Phase {

        Talking t = null;
        String accused = null;
        Set<String> players_to_vote = null;

        private Voting(Talking aThis, String accuser, String name) {
            t = aThis;
            accused = name;
            players_to_vote = new TreeSet<>(t.playing_users_set);
            players_to_vote.remove(accuser);
            players_to_vote.remove(accused);
            notvoted_message();
            sendMessage(main_channel, "Accused player is" + accused + ".Please use !yes and !no commands to vote");
        }

        void notvoted_message() {
            String ret = " Players, who hasn't voted yet:";
            for (String k : players_to_vote) {
                ret = ret + k + ",";
            }
            sendMessage(main_channel, ret);

        }

        @Override
        public void onMessage2(String channel, String sender, String login, String hostname, String message) {
            if (message.equals("!yes")) {
                players_to_vote.remove(sender);
                String ret = "Vote recorded.Currently accused:"+accused;
                sendMessage(channel, ret);
                if (players_to_vote.size() == 0) {
                    if (accused.equals(t.spy)) {
                        sendMessage(channel, "Spy was voted. Non spies won");
                    } else {
                        sendMessage(channel, "Not the spy was voted. Spy won");
                    }
                    p = new Lobby();

                } else {
                    notvoted_message();
                }

            } else if (message.equals("!no")) {
                if (players_to_vote.contains(sender)) {
                    players_to_vote.remove(sender);
                    sendMessage(channel, "voting vetoed.");
                    int players_to_vote = 0;
                    for (String t2 : t.playing_users_set) {
                        if (t.already_nominated.contains(t2) == false && t2.equals(t.spy) == false) {
                            players_to_vote++;
                        }
                    }
                    if (players_to_vote == 0) {
                        sendMessage(channel, "There are no more vetoes, spy won.");
                        p = new Lobby();
                    } else {
                        p = t;
                    }
                }
            }

        }

        private void onUserList2(String channel, TreeSet<String> unow) {

            t.onUserList2(channel, unow);
            if (p == this) {
                for (String s : new TreeSet<>(players_to_vote) ) {
                    if (unow.contains(s) == false) {
                        onMessage2(channel, s, s, s, "!yes");
                    }
                }
            }

        }
    }

    public synchronized void onUserList(String channel,
            User[] users) {
        p.onUserList(channel, users);
    }

    public synchronized void onPrivateMessage(String sender,
            String login,
            String hostname,
            String message) {
        p.onPrivateMessage(sender, login, hostname, message);
    }

    public synchronized void onMessage(String channel, String sender,
            String login, String hostname, String message) {
        //if (message.equalsIgnoreCase("time")) 
        {
                           //getUsers(channel);
            // String time = new java.util.Date().toString();
            //sendMessage(channel, sender + "|"+login+"|"+channel+": The time is now " + time);
            p.onMessage(channel, sender, login, hostname, message);;
        }
    }

    Phase p = new Lobby();
    
    public String[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line.toLowerCase());
        }
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }
    
}
