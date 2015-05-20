
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
        if(teamMates.size()==0){
            ServerThread.cohorts.remove(this);
            return;
        }
        if(t.isAdmin()){
            teamMates.get(0).makeAdmin();
        }
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
        //maxMafia=2;
        maxMafia = (int)((0.25*teamMates.size())+0.5);
        isNight = true;
        broadcast(" ");
        broadcast("The game has begun!");
        broadcast("Current players:");
        for(ServerThread t : teamMates)
        {broadcast(t.getName());}
        broadcast(" "); 
        broadcast("@It's the first night.");
        broadcast("@ ");
        broadcast(   "@Go kill, mafia! Go investigate, detective! Go save, doctor! Go scramble, Poltergeist"  );
        broadcast("@ ");
        assign();
        listMafia();

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
            //broadcast("entered assign mafia.");
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

        //assign everyone else as a mafia
        for(ServerThread t : teamMates)
        {
            if(!t.isMafia())
            {
                t.makeVillager();
                villager++;
            }
        }
    }

    public void listMafia()
    {
        for(ServerThread t : teamMates)
        {
            if(t.isMafia())
            {
                if(mafia>1)
                {
                    t.say("Living mafia:");
                }
                else if(mafia==1)
                {
                    t.say("Right now, you are the only mafia!");
                }
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
        broadcast(" ");
        broadcast("@ ");
        int rand = (int)(Math.random())*3 + 1;
        if(!dead.equals("No one"))
        {
            if(rand==1)
            {
                broadcast("@It was a beautiful fall day. " + dead + "  was finishing up at the grocery store, purchasing ingredients for a pumpkin pie they were planning on making that evening. " + dead + " thanked the cashier and headed out of the store. Strangely, the parking lot was pretty much empty and it seemed like all of the lights were out. Out of nowhere, " + dead + "  saw a bright spark and a heard a deafening sound, emanating from the flash. " + dead + " collapsed, suffering a gunshot through the skull, never to bake another pumpkin pie again. Who is responsible for this violent act?");
            }

            else if(rand==2)
            {
                broadcast("@It was a Saturday evening, and " + dead + " was out on a date at the new fancy Italian restaurant in town. Rumor has it: the food is to die for. When the waiter placed her lasagna dish in front of them, they couldn’t resist taking a bite. A second later, they fell over dead. The people around called 911 but it was too late. "+ dead + " was eating by themselves, so who could have done it?");
            }

            else
            {
                broadcast("@" + dead + "was camping with some friends in a cabin in Montana. It was getting pretty chilly, so they decided to make a fire. Realizing there wasn’t enough wood, " + dead + "volunteered to go outside to gather some for the fire. Everyone saw " + dead + " go into the woods, but no one saw them come out. Dun Dunn Dunnn. Who dun it? hehe ;)");
            }
        }
        else if(dead.equals("No one"))
        {
            int assign =(int) (Math.random())*3 + 1;
            if(assign==1)
            {
                broadcast("@It was a beautiful day. A citizen of the town was leaving the local grocery store when they were shot in the parking lot. They were losing a lot of blood, fast. The situation was dire, it looked like that poor citizen would die. Luckily, the doctor happened to be exiting the McDonald’s drive-thru in the same shopping center. The doctor saw the citizen lying in the street, and quickly rushed over to them. The doctor was able to stop the bleeding and take the citizen to the nearest hospital, saving their life.");
            }
            else if(assign==2)
            {
                broadcast("@It was a Saturday evening, and a citizen of the town was out on a date at the new, fancy Italian restaurant in town. Of course, the citizen had to impress their date by ordering the best wine on the menu. The doctor was also dining at the same place and had just exited the restroom in the back when they saw one of the waiters produce a small vial filled with green liquid from the pocket in his apron and proceed to pour the contents into a wine glass through the transparent kitchen door. Recognizing this as an attempted poisoning, the doctor burst through the kitchen door and called attention to the situation. The citizen was the intended recipient of the poison, and if not for the heroic doctor, would surely be dead at this point. The mischievous mafia watched through the tinted windows of their black SUV as the police dragged away the paid waiter. Who are the mafia?");
            }
            else
            {
                broadcast("@A citizen of the town was camping with some friends in a cabin in Montana. It was getting pretty chilly, so they decided to make a fire. Realizing there wasn’t enough wood, the citizen volunteered to go outside to gather some for the fire. Everyone saw the citizen go into the woods, but no one saw them come out. Luckily, the doctor was on their annual hike when they noticed a strange creature in the nearby river. The doctor jumped in and pulled the drowning body onto the river bank. The doctor was able to pump profuse amounts of water from the citizen’s chest, saving their life. How did the citizen end up in the river?");
            }
        }
        broadcast("@ ");
        if(will != null)
        {
            broadcast("@He/she left a will for you all: " + will   );
        }
        broadcast("@ ");
        broadcast("Current survivors:");
        for(ServerThread t : teamMates)
        {if(!t.isDead()){broadcast(t.getName());}}
        broadcast(" "); 
        broadcast("@ ");
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
        else{broadcast("@It is now morning. Vote to execute a mafia!");
            String out = "$votes:";
            for(ServerThread a : getPlayers()){
                if(!a.isDead()){out+=" "+a.getName();}
            }
            broadcast(out);
        }
        broadcast("@ ");
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
        broadcast(" ");
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
        else{
            broadcast("@ ");
            broadcast("@It is now night. Go do your thing, special characters!");}

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
        isNight=false; 
        isDawn=true; 
        for(ServerThread t : teamMates)
        {
            if(recruit.toLowerCase().equals(t.getName().toLowerCase())){r=t;}
        }
        r.out.println("The mafia would like to recruit you. Would you like to accept or deny? Note if you play a special role, you'll have to abandon your position in order to join the mafia");
        r.out.println("$aord: ");
        r.out.println(" ");
    }

    public void recruit(String answer)
    {
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
            r.out.println(" ");
            broadcastM(torecruit+" is now a mafia! The maximum ammount is "+ maxMafia+" mafia.");
            listMafia(); 
            broadcastM(" ");

        }
        else if(answer.equals("Deny"))
        {
            r.out.println(" ");
            r.out.println("You have chosen to remain your original character.");
            broadcastM(torecruit+" does not want to join your mafia team.");
            broadcastM(" ");
            r.out.println(" "); 
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
            broadcastDe("Hurry up and investigate, detective!");
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
            broadcastM(" "); 
        }
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
        boolean exists = false;
        for(ServerThread t : teamMates)
        {
            if(tbr.toLowerCase().equals(t.getName().toLowerCase())){exists = true;}
        }
        if(!exists){return false;}

        //prompts the detective to do his thing
        if(!investigated && detective==1){
            broadcastDe("Hurry up and investigate, detective!");
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
                {

                    thread.say( "You found a mafia! Now convince the other villagers!"  );
                    thread.say(" ");
                }
                else
                {

                    thread.say("This person is either not the mafia or dead. See you in the morning!"  );
                    thread.say(" ");
                }
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
            
            while(!ok){
                int voteNum = (int)(Math.random()*(teamMates.size()-numDead));
                ServerThread pos = teamMates.get(voteNum);
                tbk = pos.getName();
                if(!pos.isDead() && !tbk.equals(orig))
                    ok = true;
            }
            
            broadcast(thread.getName() + " voted for " + tbk + "!");
        }
        boolean exists = false;
        for(ServerThread t : teamMates)
        {
            if(tbk.toLowerCase().equals(t.getName().toLowerCase())){exists = true;}
        }
        if(!exists){return false;}
        if(votes<(villager+mafia) && voted.indexOf(thread)==-1)
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
        if(votes == (villager + mafia))
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
            else{night("@Vote tied. Nobody", null);}

            return true;
        }
        return true;
    }
}
