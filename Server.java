
import java.net.*;
import java.io.*;
import java.util.ArrayList;
class ServerThread implements Runnable {

    /* The client socket and IO we are going to handle in this thread */
    protected Socket         socket;
    protected PrintWriter    out;
    protected BufferedReader in;
    protected int ID;
    protected static int numClie= 0;
    protected static ArrayList<ServerThread> clientSocks = new ArrayList<ServerThread>();
    protected static ArrayList<Cohort> cohorts = new ArrayList<Cohort>();
    protected Cohort cohort;
    public String name;
    public boolean admin = false;
    public boolean mafia = false;
    public boolean dead = false;
    public boolean doc = false;
    public boolean saved = false;
    public boolean polt = false;
    public boolean scrambled = false;
    //added 
    public String will;

    //added
    public boolean investigated=false;
    public boolean detective=false;

    public ServerThread(Socket socket, int ident) {
        /* Assign local variable */
        this.socket = socket;
        this.ID = ident;
        ServerThread.clientSocks.add(this);
        /* Create the I/O variables */
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            /* Say hi to the client */
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void intro(){
        try {this.out.println(" ");
            this.out.println("Welcome to Online Mafia!");
            this.out.println("Enter your name:");
            this.out.println(" ");

            name = this.in.readLine();
            if (name == null) {
                this.in.close();
                this.out.close();
                this.socket.close();
                return;
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void cohortMenu(){
        try {
            this.out.println(" ");
            this.out.println("Active games:");
            this.out.println(" ");
            for(Cohort t: ServerThread.cohorts)
            {
                this.out.println(t.getID());
            }
            boolean horted = false;
            if(ServerThread.cohorts.size()>0){this.out.println(" ");}
            horting: while(!horted){
                this.out.println("To join a game, use the join command: 'join game_name'");
                this.out.println(" ");
                this.out.println("To create a new game, use the create command: 'create game_name'");
                this.out.println(" ");
                this.out.println("To exit, type 'exit'");
                this.out.println(" ");

                String cmd = this.in.readLine();
                if (cmd == null) {
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }
                String[] cmds = cmd.split(" ");

                if(cmd.toLowerCase().equals("exit"))
                {
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }

                if(cmds[0].toLowerCase().equals("join"))
                {

                    for(Cohort t: ServerThread.cohorts)
                    {
                        if(t.getID().toLowerCase().equals(cmds[1].toLowerCase()))
                        {
                            this.out.println("Enter Password:");
                            String pass = this.in.readLine();
                            if(pass.equals(t.pass)){
                                for(ServerThread a : t.teamMates)
                                {
                                    if(this.getName().toLowerCase().equals(a.getName().toLowerCase())){
                                        this.out.println("Somebody already has that name. We'll call you "+ this.getName()+"2");
                                        this.name=this.getName()+"2";
                                    }
                                }

                                t.add(this);
                                this.cohort = t;

                                horted = true;
                                break horting;
                            }
                        }
                    }
                    this.out.println("There isn't a game with that name.");
                    this.out.println(" ");
                }
                else if(cmds[0].toLowerCase().equals("create"))
                {
                    boolean another = false;
                    for(Cohort t: ServerThread.cohorts)
                    {
                        if(t.getID().toLowerCase().equals(cmds[1].toLowerCase()))
                        {
                            this.out.println("There's already a game with that name.");
                            another = true;
                        }
                    }
                    if(!another)
                    {
                        this.out.println("Choose a password:");
                        String pass = this.in.readLine();
                        cohort = new Cohort(cmds[1], pass);
                        cohort.add(this);
                        ServerThread.cohorts.add(cohort);
                        horted = true;
                        dead = false;
                        admin = true;
                    }
                }
                else{this.out.println("Command not understood");
                    this.out.println(" ");}
            }

            /*put valid commands here*/
            this.out.println(" ");
            if(admin)
            {
                this.out.println("As an admin, kick players using the kick command: 'kick player_name'");
                this.out.println("Start the game with 'start'");
                this.out.println(" ");
            }
            this.out.println("Enter a will(this can be changed when the game starts):");
            this.out.println(" ");
            will=this.in.readLine();
            this.out.println("This will be revealed to everyone, once you have died!");
            this.out.println(" ");
            if (will == null) {
                this.in.close();
                this.out.close();
                this.socket.close();
                return;
            }

            this.out.println("To leave to the main menu, type leave.");
            this.out.println("To exit, type exit.");
            this.out.println("To change your will, type 'changeWill new_will'");
            this.out.println("NOTE: Wills cannot be changed after death");
            this.out.println("To vote to kill a player during the day, type 'vote player_name'");
            this.out.println("NOTE: Votes cannot be undone");
            this.out.println(" ");

            cohort.sayAll(name + " has joined the game!", this);
            if(cohort.isStarted())
            {
                this.out.println(" ");
                cohort.numDead++;
                this.out.println("You're a spectator. Wait for the round to end for your chance to play!");
                dead = true;
            }
        }catch (IOException e) {
            System.out.println("IOException: " + e);
            return;
        }
    }

    public int getID(){
        return this.ID;
    }

    public String getName(){
        return name;
    }

    public String getWill(){
        return will;
    }

    public void setWill(String msg){
        will=msg;
        this.out.println("Your new will is: "+msg);
        this.out.println(" ");
    }

    public void say(String msg)
    {
        this.out.println(msg);
    }

    public void run() {
        /* Our thread is going to read lines from the client and  them back.
        It will continue to do this until an exception occurs or the connection ends

         */
        this.intro();
        /* Some debug */
        this.cohortMenu();

        while (true) {
            try {
                /* Get string from client */

                String fromClient = this.in.readLine();
                if (cohort==null)
                {
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }
                /* If null, connection is closed, so just finish */
                if (fromClient == null || fromClient.toLowerCase().equals("exit")||fromClient.toLowerCase().equals("leave")) {
                    if(cohort!=null){
                        cohort.sayAll(name+" has left.", this);}

                    this.out.println(" ");
                    this.out.println("You have left the game.");
                    if(cohort.isStarted()&& this.isMafia() )
                    {
                        cohort.mafia--;
                        cohort.teamMates.remove(this);
                        cohort.remove(this);
                        if (cohort.isStarted() && cohort.mafia==0)
                        {   
                            cohort.broadcast( "The last Mafia has left.The villagers win!"  );
                            cohort.tellAdmin("As admin, start the game again with start");
                            cohort.stop();
                        } 
                        this.cohort = null;
                    }
                    else if(cohort.isStarted())
                    {
                        cohort.villager--; 
                        cohort.teamMates.remove(this);
                        cohort.remove(this);
                        if( cohort.isStarted() && cohort.villager==0)
                        {
                            cohort.broadcast(  "The last villager has left.The mafia win!"  );
                            cohort.tellAdmin("As admin, start the game again with start");
                            cohort. stop();
                        }

                        this.cohort = null;
                    }

                    this.cohortMenu();
                    if(fromClient.toLowerCase().equals("exit")||!cohort.isStarted())
                    {
                        this.in.close();
                        this.out.close();
                        this.socket.close();
                        return;
                    }
                }

                String[] input = fromClient.split(" ");

                input[0] = input[0].toLowerCase();

                if(input[0].equals("kick")){
                    if(admin)
                    {
                        cohort.kick(input[1]);
                    }
                    else{this.out.println("You're not an admin");
                    }
                }

                else if(input[0].equals("start") && admin && !cohort.isStarted())
                {
                    cohort.start();
                }
                else if(input[0].equals("changewill"))
                {
                    if(!dead)
                    {
                        will="";
                        for(int x=1; x< input.length; x++)
                        {
                            will+=input[x]+" ";
                        }
                        this.out.println("Your new will is: "+will);
                        this.out.println(" ");
                    }
                }
                else if(input[0].equals("scream") && admin)
                {
                    cohort.sayAll((fromClient.substring(fromClient.indexOf(' ')+1)), this);
                }

                else if(!cohort.isStarted())
                {   cohort.sayAll(name + ": "+ fromClient,this);}

                else if(cohort.isNight())
                {
                    if (mafia && !dead){
                        if(cohort!=null){cohort.sayMafia(name + ": " +fromClient, this);}
                    }
                    if(input[0].equals("investigate")&& detective && !dead)
                    {
                        cohort.investigate(input[1], this);
                    }
                    /*
                    if (doc && !dead)
                    {
                    if(cohort != null){cohort.sayDoc(name + ": "+ fromClient, this);}
                    }
                    if(polt && !dead)
                    {
                    if(cohort != null){cohort.sayPolt(name +": " +fromClient, this);}
                    }
                     */
                    if(input[0].equals("save") && doc && !dead)
                    {
                        save(input[1]);
                    }
                    if(input[0].equals("scramble") && polt && !dead)
                    {
                        scramble(input[1]);
                    }
                    if(input[0].equals("kill") && mafia && !dead)
                    {
                        boolean a = cohort.votekill(input[1], this);
                        if(!a){this.out.println("They're already dead. Choose again.");}
                    }
                }

                else if(!cohort.isNight())
                {
                    if(input[0].equals("vote") && !dead)
                    {
                        boolean a = cohort.vote(input[1], this);
                        if(!a){this.out.println("They're already dead. Choose again.");}
                        else
                        {
                            cohort.sayAll(( this.getName() + " voted for " + input[1] + "!"  ), this);
                            this.out.println( "Thanks for voting for "+ input[1]+"!"  );
                            this.out.println(" ");
                        }
                    }
                    else if(!dead){cohort.sayAll(name + ": " +fromClient, this);}
                }
            } catch (IOException e) {
                /* On exception, stop the thread */
                System.out.println("IOException: " + e);
                return;
            }
        }
    }

    public void kick()
    {
        cohort = null;
        this.out.println(" ");
        this.out.println( "You have been kicked from the game."  );
        cohortMenu();
    }

    public boolean isDead(){return dead;}

    public void kill(){
        this.reset();
        dead = true;
        if(isPolt())
            cohort.livePolt = false;
        if(isDoc())
            cohort.liveDoc = false;
        cohort.numDead++;
        this.out.println(" ");
        this.out.println( "You died! Wait for next round!"  );
    }

    public synchronized void save(String toSave)
    {
        for(ServerThread t: cohort.teamMates)
        {
            if(t.getName().toLowerCase().equals(toSave.toLowerCase()))
            {
                if(t.isDead()){this.out.println("That Player is dead. Try another.");}
                else
                {
                    t.saved = true;
                    cohort.pSaved = t;
                    this.out.println( "You have saved someone! You'll find out if he/she was endangered in the morning!");
                    this.out.println(" ");
                    for(ServerThread cl: cohort.teamMates)
                    {
                        if(cl.isMafia())
                        {
                            try{
                                cohort.objDoc.notify();
                            }catch(Exception e){}
                        }
                    }
                    if((cohort.investigated||cohort.detective==0) && (cohort.tokill!=null) && (cohort.someScram==true || cohort.livePolt==false))
                    {
                        cohort.killPerson(cohort.tokill);
                    }
                    return;
                }
            }
        }
        this.out.println("There is no player with that name. Try another.");
    }

    public synchronized void scramble(String toScram)
    {
        for(ServerThread t: cohort.teamMates)
        {
            if(t.getName().toLowerCase().equals(toScram.toLowerCase()))
            {
                if(t.isDead()){this.out.println("That Player is dead. Try another."); return;}
                else
                {
                    t.scrambled = true;
                    cohort.someScram = true;
                    this.out.println( "You have scrambled! See you in the morning!"  );
                    this.out.println(" ");
                    for(ServerThread cl: cohort.teamMates)
                    {
                        if(cl.isMafia())
                        {
                            try{
                                cohort.objPol.notify();
                            }catch(Exception e){}
                        }
                    }
                    if((cohort.investigated||cohort.detective==0) && (cohort.tokill!=null) && (cohort.pSaved!=null || cohort.liveDoc==false))
                    {
                        cohort.killPerson(cohort.tokill);
                    }
                    return;
                }
            }
        }
        this.out.println("There is no player with that name. Try another.");
    }

    public void reset()
    {
        mafia = false;
        dead = false;
        doc = false;
        saved = false;
        polt = false;
        scrambled = false;
        detective=false;
    }

    public boolean isMafia(){return mafia;}

    public boolean isAdmin(){return admin;}

    public boolean isDoc(){return doc;}

    public boolean isSaved(){return saved;}

    public boolean isPolt(){return polt;}

    public boolean isScrambled(){return scrambled;}

    public void oppoSave()
    {
        saved = !saved;
    }

    public boolean isDetective(){return detective;}

    public void makeDetective()
    {
        detective=true; 
        this.out.println( "You're the detective.Investigate somebody by typing 'investigate player_name'"  );
        this.out.println(" ");

    }

    public void makeDoctor()
    {
        doc = true;
    }

    public void makeMafia()
    {
        mafia = true;
        this.out.println("You're the mafia. Kill somebody by typing 'kill player_name'");
        this.out.println("You can also talk to the other mafia now!");
        this.out.println(" ");
    }

    public void polterate()
    {
        polt = true;
        this.out.println( "You're the Poltergeist. Your job is simply to make the villagers' job more difficult every day."  );
        this.out.println("Each night, you can make someone's vote random by typing 'scramble player_name'");
        this.out.println("While you don't actually have anyone to talk to, talking to yourself would complete the illusion of a mad ghost.");
        this.out.println(" ");
    }

    public void makeVillager()
    {
        mafia = false;
        this.out.println( "You're a villager"  );
        this.out.println(" ");
    }
}

public class Server {

    public static void main(String [] args) {

        /* Check port exists */

        /* This is the server socket to accept connections */
        ServerSocket serverSocket = null;

        /* Create the server socket */
        try {
            serverSocket = new ServerSocket(10101);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }

        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (true) {
            try {
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();

                /* Create a thread for it and start! */
                ServerThread clientThread = new ServerThread(clientSocket, ServerThread.numClie);
                ServerThread.numClie++;
                new Thread(clientThread).start();

            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
        }
    }
}
