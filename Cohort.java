
import java.util.*;
public class Cohort
{
    public String teamName;
    public ArrayList<ServerThread> teamMates;
    public boolean started = false;
    public boolean isNight;
    public boolean isDawn; 

    public int mafia=0;
    public int deadMafia=0;
    public int maxMafia=0;
    public boolean recruit=true; 

    public int villager=0;
    public int detective = 0;
    public boolean livePolt = true;
    public boolean liveDoc = true;

    public boolean investigated=false;//whether the detective investigated
    public ServerThread pSaved = null;//the person the doctor saved
    public boolean someScram = false;//whether the geist has scammed
    public boolean killed=false;//the name of the person the mafia have killed
    public String killedWill=null;//the person mafia's killed's will 

    public String pass;

    public int assign;

    public int votes=0;
    public String tokill=null;//the person the mafia wants to kill
    public ArrayList<String> toKill = new ArrayList<String>();
    public ArrayList<String> toVote = new ArrayList<String>();
    public ArrayList<ServerThread> voted = new ArrayList<ServerThread>();
    public String torecruit=null;
    public ArrayList<String> toRecruit = new ArrayList<String>();
    public ServerThread r;

    public Object objDoc, objPol;
    public int numDead = 0;

    public static final String ANSI_RESET="\u001B[0m";
    public static final String ANSI_BLUE="\u001B[34m";
    public Cohort(String name, String password)
    {
        teamName=name;
        teamMates = new ArrayList<ServerThread>();
        pass = password;
    }

    public boolean add(ServerThread cl)
    {
        for(ServerThread t : teamMates)
        {
            if(t.getName().toLowerCase().equals(cl.getName().toLowerCase()))
            {
                return false;
            }
        }
        teamMates.add(cl);
        if(isStarted())
            numDead++;
        return true;
    }

    public void kick(String tbk)
    {
        for(ServerThread t : teamMates)
        {
            if(tbk.toLowerCase().equals(t.getName().toLowerCase())){
                t.say(  "You have been kicked for misbehaving!"  );
                broadcast(t.getName()+" has been kicked.");

                if(isStarted()&& t.isMafia() )
                {
                    mafia--;
                    deadMafia++; 
                    teamMates.remove(t);
                    remove(t);
                    if (isStarted() && mafia==0)
                    {   
                        broadcast(  "@The last Mafia has left.The villagers win!"  );
                        tellAdmin("@As admin, start the game again with start");
                        stop();
                    } 
                    t.cohort = null;
                }
                else if(isStarted())
                {
                    villager--; 
                    teamMates.remove(t);
                    remove(t);
                    if(isStarted() && villager==0)
                    {
                        broadcast(   "@The last villager has left.The mafia win!"  );
                        tellAdmin("@As admin, start the game again with start");
                        stop();
                    }

                    t.cohort = null;
                }
                t.kick();
            }
        }   

        return;
    }

    public ArrayList<ServerThread> getPlayers()
    {
        return teamMates;
    }

    public void remove(ServerThread t)
    {
        teamMates.remove(t);
    }

    public String getID(){
        return teamName;
    }

    public void start(){
        if(teamMates.size() < 4)
        {
            broadcast(" ");
            broadcast("Not enough Players to begin the game.");
            broadcast(" ");
            return;
        }
        started = true;
        isNight = true;
        broadcast(" ");
        broadcast("The game has begun!");
        broadcast("Current players:");
        for(ServerThread t : teamMates)
        {broadcast(t.getName());}
        broadcast("@It's the first night.");
        broadcast(" ");
        broadcast(   "@Go kill, mafia!Go investigate, detective! Go save, doctor! Go scramble, Poltergeist"  );
        broadcast(" ");
        assign();
        listMafia();
        //maxMafia=3;
        maxMafia = (int)((0.25*teamMates.size())+0.5);

        String out = "$kill:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead() && !a.isMafia()){out+=" "+a.getName();}
        }
        broadcastM(out);

        out = "$save:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastDo(out);

        out = "$polt:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastP(out);

        out = "$inv:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastDe(out);
    }

    public void assign()
    {
        //assigns the mafia
        //added
        if(recruit==false)
        {
            mafia = 0;
            while(mafia < maxMafia)
            {
                int target = (int)((Math.random()*teamMates.size()));
                if(teamMates.get(target).isMafia() || teamMates.get(target).isDead())
                    target = (int)((Math.random()*teamMates.size()));
                else
                {
                    teamMates.get(target).makeMafia();
                    teamMates.get(target).out.println(  "@You're a Mafia! You and your allies have to kill all the villagers to win."  );
                    mafia++;
                }
            }
        }
        else
        {
            mafia=0;
            int target = (int)((Math.random()*teamMates.size()));
            if(teamMates.get(target).isMafia() || teamMates.get(target).isDead())
                target = (int)((Math.random()*teamMates.size()));
            else
            {
                teamMates.get(target).makeMafia();
                teamMates.get(target).out.println(  "@You're a Mafia! You and your allies have to kill all the villagers to win."  );
                mafia++;
            }
        }
        //done
        //assign everyone else as a mafia
        for(ServerThread t : teamMates)
        {
            if(!t.isMafia())
            {
                t.makeVillager();
                villager++;
            }
        }

        //assign doctor 
        ServerThread target = teamMates.get((int)((Math.random()*teamMates.size())));
        while(target.isMafia() || target.isDead())
        {
            target = teamMates.get((int)((Math.random()*teamMates.size())));
        }
        target.makeDoctor();
        target.out.println(  "@You're the Doctor! You get to save whomever you'd like each night."  );
        target.out.println("@To save someone type 'save player_Name'");
        liveDoc=true;
        //assign pol
        while(target.isMafia() || target.isDoc() || target.isDead())
        {
            target = teamMates.get((int)((Math.random()*teamMates.size())));
        }
        target.polterate();

        //assign Detective
        while(target.isMafia() || target.isPolt() || target.isDoc())
        {
            target = teamMates.get((int)((Math.random()*teamMates.size())));
        }
        target.makeDetective();
    }

    public void listMafia()
    {
        for(ServerThread t : teamMates)
        {
            if(t.isMafia())
            {
                t.say("Living mafia:");
                for(ServerThread s : teamMates)
                {
                    if(s.isMafia())
                    {
                        t.say(s.getName());
                    }
                }
            }
        }
    }

    public void stop(){
        started = false;
        isNight = false;
        numDead = 0;
        for(int i=0; i<teamMates.size(); i++)
        {
            teamMates.get(i).reset();
        }
    }
    //point in game
    public boolean isStarted(){return started;}

    public boolean isNight(){return isNight;}

    //ways to speak
    public void sayAll(String msg, ServerThread thread)
    {
        for(ServerThread t : teamMates)
        {
            if(thread.getID()!=(t.getID()) && !t.isDead()){t.say(msg);}
            else if(t.isDead() && thread.isMafia())
            {t.say("*Mafia* "+msg);}
            else if(t.isDead()){t.say(msg);}
            else if(t.isDead() && thread.isDetective())
            { t.say("*Detective* "+msg);}
            else if(t.isDead() && thread.isPolt())
            { t.say("*Poltergeist* "+msg);}
            else if(t.isDead() && thread.isDoc())
            { t.say("*Doctor* "+msg);}
        }
    }

    public void broadcast(String msg)
    {
        for(ServerThread t : teamMates)
        {   t.say(msg);
        }
    }

    public void broadcastM(String msg)
    {
        for(ServerThread t : teamMates)
        {   if(t.isMafia()){t.say(msg);}
        }
    }

    public void broadcastDo(String msg)
    {
        for(ServerThread t : teamMates)
        {   if(t.doc){t.say(msg);}
        }
    }

    public void broadcastP(String msg)
    {
        for(ServerThread t : teamMates)
        {   if(t.polt){t.say(msg);}
        }
    }

    public void broadcastDe(String msg)
    {
        for(ServerThread t : teamMates)
        {   if(t.detective){t.say(msg);}
        }
    }

    public void sayMafia(String msg, ServerThread thread)
    {
        for(ServerThread t : teamMates)
        {
            if(thread.getID()!=(t.getID()) && t.isMafia() && !t.isDead())
            {t.say(msg);}
            else if(t.isDead())
            {t.say("*Mafia* "+msg);}
        }
    }

    /** I don't know what this is for? There is only one doctor and pol
    public void sayDoc(String msg, ServerThread thread)
    {
    for(ServerThread t : teamMates)
    {
    if(thread.getID()!=(t.getID()) && t.isDoc() && !t.isDead())
    {t.say(msg);}
    else if(t.isDead())
    {t.say("*Doctor* "+msg);}
    }
    }
    public void sayPolt(String msg, ServerThread thread)
    {
    for(ServerThread t : teamMates)
    {
    if(thread.getID()!=(t.getID()) && t.isPolt() && !t.isDead())
    {t.say(msg);}
    else if(t.isDead())
    {t.say("*Poltergeist* "+msg);}
    }
    }
     */
    public void tellAdmin(String msg)
    {
        for(ServerThread t : teamMates)
        {
            if(t.isAdmin())
            {
                t.say(msg);
            }
        }
    }

    public void morning(String dead, String will)
    {
        isDawn=false; 
        broadcast("$day");
        String out = "$votes:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcast(out);

        broadcast(" ");
        broadcast( "@"+dead + " was killed by the mafia last night."  );
        if(dead.equals("No one"))
        {
            broadcast( "@ Doctor saved" );}
        if(will != null)
        {
            broadcast("@He/she left a will for you all: " + will   );
        }
        broadcast("Current survivors:");
        for(ServerThread t : teamMates)
        {if(!t.isDead()){broadcast(t.getName());}}
        broadcast(" ");
        if(villager==0)
        {
            broadcast(  "@All villagers killed. The mafia win!"   );
            tellAdmin("@As admin, start the game again with start");
            stop();
        }
        else if (mafia==0)

        {
            broadcast(  "@All mafia killed. The vilalagers win!"  );
            tellAdmin("@As admin, start the game again with start");
            stop();
        }
        else{broadcast("@It is now morning. Vote to execute a mafia!");}
        broadcast(" ");
        isNight = false;
    }

    public void night(String dead, String will)
    {
        broadcast("$night");

        String out = "$kill:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead() && !a.isMafia()){out+=" "+a.getName();}
        }
        broadcastM(out);

        out = "$save:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastDo(out);

        out = "$polt:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastP(out);

        out = "$inv:";
        for(ServerThread a : getPlayers()){
            if(!a.isDead()){out+=" "+a.getName();}
        }
        broadcastDe(out);

        //broadcast who is dead and their will
        broadcast(" ");
        broadcast(dead + " was executed."  );
        if(will!=null)
        {
            broadcast("He/she left a will for you all: " + will);
        }

        //broadcast who is alive -> Highlight this 
        broadcast("Current survivors:");
        for(ServerThread t : teamMates)
        {
            if(!t.isDead()){
                broadcast(t.getName());
            }
        }
        broadcast(" ");
        listMafia();

        //game over stuff
        if(villager==0)
        {
            broadcast(  "@All villagers killed. The mafia win!"  );
            tellAdmin("@As admin, start the game again with start");
            stop();
        }
        else if (mafia==0)
        {
            broadcast(  "@All mafia killed. The villagers win!"   );
            tellAdmin("@As admin, start the game again with start");
            stop();
        }
        else{broadcast("It is now night. Go mafia!");}

        //tell special people to do their thing
        for(ServerThread t : teamMates)
        {
            if(t.isMafia())
                t.out.println("Pick someone to kill or chat with the other Mafia.'");
            if(t.isPolt())
                t.out.println("Pick someone to scramble!");
            if(t.isDoc())
                t.out.println("Who will you save?");
            if(t.isDetective())
                t.out.println("Who will you investigate?");
        }
        broadcast(" ");

        isNight = true;
    }

    public void dawn (String recruit)
    {
        broadcast("entered dawn");
        isNight=false; 
        isDawn=true; 
        for(ServerThread t : teamMates)
        {
            if(recruit.toLowerCase().equals(t.getName().toLowerCase())){r=t;}
        }
        r.out.println("The mafia would like to recruit you. Would you like to accept or deny? Note if you play a special role, you'll have to abandon your position in order to join the mafia");
        r.out.println("$aord: ");
    }

    public void recruit(String answer)
    {
        broadcast("entered recruit");
        if(answer.equals("Accept"))
        {
            r.makeMafia(); 
            mafia++;
            if(r.isDetective())
            {   
                detective--;
                villager--;
            }
            else if(r.isDoc())
            {
                liveDoc=false;
                villager--;
            }
            else if(r.isPolt())
            {
                livePolt=false;
                villager--;
            }
            else
                villager--;
            r.setMafia(); 
            broadcastM(torecruit+" is now a mafia! There are now" + mafia +" and the maximum ammount is+"+ maxMafia);
            listMafia(); 

        }
        else if(answer.equals("Deny"))
        {
            r.out.println("You have chosen to remain your original character.");
            broadcastM(torecruit+" does not want to join your mafia team.");

        }
        else
        {
            broadcastM("Something went wrong. Please try to recruit again tomorrow night."); 
        }
        votes=0; 
        voted = new ArrayList<ServerThread>();
        torecruit=null;
        toRecruit = new ArrayList<String>(); 
        r=null; 
        killPerson(tokill);
    }

    public synchronized boolean votekill(String tbk, ServerThread thread){
        //if someone doesn't exist...
        boolean exists = false;
        for(ServerThread t : teamMates)
        {
            if(tbk.toLowerCase().equals(t.getName().toLowerCase())){exists = true;}
        }
        if(!exists){return false;}

        //prompts the detective to do his thing
        if(!investigated && detective==1){
            broadcast("Hurry up and investigate, detective!");
        }

        //have the mafia vote to recruit
        if(votes<mafia && voted.indexOf(thread)==-1)
        {
            for(ServerThread t : teamMates)
            {
                if(tbk.toLowerCase().equals(t.getName().toLowerCase()) && t.isDead()){
                    return false;}
            }
            toKill.add(tbk);
            votes++;
            voted.add(thread);
        }

        //once they agree
        if(votes == mafia)
        {
            int min = 0;

            for(String str : toKill)
            {
                int x = Collections.frequency(toKill, str);
                if(x>min)
                {
                    min = x;
                    tokill = str;//set the person they want to tokill
                }
            }
            broadcastM( "You have agreed to kill "+ tokill+"! Find out if that pesky doctor saved him in the morning!"  );

        }
        broadcastM("VK- # of votes: "+votes+";# of mafia: "+mafia+";# of maxMafia: "+maxMafia);
        //added
        if((votes==mafia) && (recruit==true) && (mafia+deadMafia)<maxMafia)
        {
            votes=0;
            voted = new ArrayList<ServerThread>();
            broadcastM("Now choose someone to recruit someone to your mafia team!");
            String out="$recruit:";
            for(ServerThread a : getPlayers()){
                if(!a.isDead() && !a.isMafia()){out+=" "+a.getName();}
            }
            broadcastM(out);
            return true; 
        }
        if((votes==mafia) && (investigated||detective==0) && (pSaved!=null|| liveDoc==false) && (someScram==true || livePolt==false))
        {
            killPerson(tokill);
        }

        return true;
    }

    public synchronized boolean voteRecruit(String tbr, ServerThread thread){
        //if someone doesn't exist...

        broadcast("entered voteRecruit");
        boolean exists = false;
        for(ServerThread t : teamMates)
        {
            if(tbr.toLowerCase().equals(t.getName().toLowerCase())){exists = true;}
        }
        if(!exists){return false;}

        //prompts the detective to do his thing
        if(!investigated && detective==1){
            broadcast("Hurry up and investigate, detective!");
        }

        //have the mafia agree on someone to kill
        if(votes<mafia && voted.indexOf(thread)==-1)
        {
            for(ServerThread t : teamMates)
            {
                if(tbr.toLowerCase().equals(t.getName().toLowerCase()) && t.isDead()){
                    return false;}
            }
            toRecruit.add(tbr);
            votes++;
            voted.add(thread);
        }
        broadcastM("VR- # of votes: "+votes+";# of mafia: "+mafia+";# of maxMafia: "+maxMafia);
        //once they agree
        if(votes == mafia)
        {
            int min = 0;

            for(String str : toRecruit)
            {
                int x = Collections.frequency(toRecruit, str);
                if(x>min)
                {
                    min = x;
                    torecruit = str;//set the person they want to tokill

                }
            }
            broadcastM( "You have agreed to recruit someone! Find out if they agree to join the mafia at dawn!"  );

        }

        if((votes==mafia)&&(torecruit!=null)&&(investigated||detective==0) && (pSaved!=null|| liveDoc==false) && (someScram==true || livePolt==false))
        {            
            dawn(torecruit);
        }
        return true;
    } 

    public void killPerson(String tbk)
    {

        String killWill= new String();
        for(ServerThread t : teamMates)
        {
            if(tbk.toLowerCase().equals(t.getName().toLowerCase()))
            {

                if(!t.equals(pSaved))//if this isn't the person the doctor saves
                {
                    killWill=t.getWill();
                    if(t.isDetective())
                    {   
                        detective=0;
                        villager--;
                    }

                    else if(t.isMafia())
                    {
                        mafia--;
                        deadMafia++; 
                    }
                    else if(t.isDoc())
                    {
                        liveDoc=false; 
                        villager--;
                    }
                    else if(t.isPolt())
                    {
                        livePolt=false;
                        villager--;
                    }
                    else
                    {
                        villager--;
                    }
                    t.kill();
                }
                else
                {
                    tokill = "No one";
                    killWill = null;
                }

            }
        }  
        //reset night time actions
        pSaved = null;
        investigated=false;
        toKill=null;
        someScram = false;

        votes = 0;
        toKill = new ArrayList<String>();
        voted = new ArrayList<ServerThread>();

        morning(tokill, killWill);

    }

    public void investigate (String tbi, ServerThread thread){
        for(ServerThread t : teamMates)
        {

            if(tbi.equals(t.getName())){
                //added

                if(t.isMafia())
                    thread.say( "You found a mafia! Now convince the other villagers!"  );
                else
                    thread.say("This person is either not the mafia or dead. See you in the morning!"  );

            }
        }
        investigated=true;
        if((votes==mafia)&&(pSaved!=null||liveDoc==false) && (tokill!=null) && (torecruit!=null) && (someScram==true || livePolt==false))
        {
            dawn(torecruit);
        }
        else if((votes==mafia)&&(pSaved!=null||liveDoc==false) && (tokill!=null) && (someScram==true || livePolt==false))
        {
            killPerson(tokill); 
        }
        return;
    }

    public boolean vote(String tbk, ServerThread thread){
        if(thread.isScrambled())
        {
            boolean ok = false;
            String orig = tbk;
            broadcast(thread.getName() + " voted for " + tbk + "!");
            while(!ok){
                int voteNum = (int)(Math.random()*(teamMates.size()-numDead));
                ServerThread pos = teamMates.get(voteNum);
                tbk = pos.getName();
                if(!pos.isDead() && !tbk.equals(orig))
                    ok = true;
            }
        }
        boolean exists = false;
        for(ServerThread t : teamMates)
        {
            if(tbk.toLowerCase().equals(t.getName().toLowerCase())){exists = true;}
        }
        if(!exists){return false;}
        if(votes<villager+mafia && voted.indexOf(thread)==-1)
        {
            for(ServerThread t : teamMates)
            {
                if(tbk.toLowerCase().equals(t.getName().toLowerCase())&& t.isDead()){
                    return false;}
            }
            toKill.add(tbk);
            votes++;
            voted.add(thread);
        }
        if(votes == villager + mafia)
        {
            int min = 0;
            String kill = new String();
            boolean two = false;
            for(String str : toKill)
            {
                int x = Collections.frequency(toKill, str);
                if(x>min)
                {
                    min = x;
                    kill = str;
                    two = false;
                }
                if(x==min && !str.equals(kill)){
                    two=true;}
            }
            String killWill=new String();
            for(ServerThread t : teamMates)
            {
                if(kill.toLowerCase().equals(t.getName().toLowerCase())&&!two){

                    t.kill();
                    killWill = t.getWill();
                    if(t.isDetective())
                    {
                        detective=0;
                        villager--;
                    }
                    else if(t.isMafia())
                    {
                        mafia--;
                        deadMafia++; 
                    }
                    else if(t.isDoc())
                    {
                        liveDoc=false; 
                        villager--;
                    }
                    else if(t.isPolt())
                    {
                        livePolt=false;
                        villager--;
                    }
                    else{villager--;}
                }
            }
            votes = 0;
            toKill = new ArrayList<String>();
            voted = new ArrayList<ServerThread>();

            if(!two){night(kill, killWill);}
            else{night("@Vote tied. Nobody"   , null);}

            return true;
        }
        return true;
    }
}
