import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

class clientThread implements Runnable {
    protected Socket         socket;
    protected PrintWriter    out;
    protected BufferedReader in;
    protected int ID;
    protected Client client;

    public clientThread(Client client) {
        /* Assign local variable */
        this.client=client;
        try {
            socket = new Socket(
                "ec2-52-8-76-50.us-west-1.compute.amazonaws.com", 10101);
            /* Create the I/O variables */
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            /* Say hi to the client */
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run(){
        while(true){
            try{
                String fromServer = this.in.readLine();
                if(fromServer!=null){
                    client.update(fromServer);
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e);
                return;
            }
        }
    }

    public void tellServer(String msg){
        this.out.println(msg);
    }
}

public class Client extends JFrame
{
    private static final String TITLE="Mafia Game";
    private static final int WIDTH=1920/2;
    private static final int HEIGHT=1080;

    private Container content;
    private JLabel result;
    private JLabel t;
    private JButton[] cells;
    private JButton[] votes = new JButton[100];
    private voteHandler[] voters;
    private JPanel buttonPanel;
    //private JButton initButton;
    private JLabel title;
    private JTextField chat;
    private JPanel all;
    private JLabel text;
    private JPanel holder;

    public final clientThread thread = new clientThread(this);

    private ExitButtonHandler exitHandler;
    private InitButtonHandler initHandler;
    private TextHandler textHandler;

    int vilmafdocdetpol;
    private boolean O;
    private boolean gameOver;
    private int font = 20;

    public Client()
    {
        //Necessary initialization code
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Get content pane
        content=getContentPane();
        content.setBackground(Color.blue.darker().darker());

        //Set layout
        //content.setLayout(new GridLayout(1,2));
        BorderLayout layout = new BorderLayout();
        JPanel all = new JPanel();
        all.setLayout(layout);

        //content.setLayout(layout);
        layout.setHgap(20);
        layout.setVgap(20);
        all.setBorder(BorderFactory.createEmptyBorder(50,30,50,30)); 
        all.setOpaque(false);
        content.add(all);

        title=new JLabel("Mafia!", SwingConstants.CENTER);
        title.setForeground(Color.white);
        title.setFont(new Font("Arial",0, 50));
        all.add(title, BorderLayout.PAGE_START);

        result=new JLabel("<html></html>", SwingConstants.LEFT);
        result.setVerticalAlignment(JLabel.BOTTOM);
        result.setForeground(Color.white);
        result.setFont(new Font("Arial",0, font));
        all.add(result, BorderLayout.CENTER);

        chat=new JTextField("", 2);
        textHandler=new TextHandler();
        chat.addActionListener(textHandler);
        chat.setFont(new Font("Arial",0, font));
        all.add(chat, BorderLayout.PAGE_END);

        holder = new JPanel();
        BorderLayout borl = new BorderLayout();
        borl.setVgap(15);
        holder.setLayout(borl);
        holder.setBackground(Color.blue.darker());
        text = new JLabel("Vote to Execute:");
        text.setForeground(Color.white);
        text.setFont(new Font("Arial",50, font));
        holder.add(text, BorderLayout.PAGE_START);
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.blue.darker());
        //buttonPanel.setOpaque(false);

        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
        holder.add(buttonPanel, BorderLayout.CENTER);
        holder.setBorder(BorderFactory.createEmptyBorder(20,10,20,10)); 
        all.add(holder, BorderLayout.LINE_START);

        //Create init and exit buttons and handlers
        /**
        exitButton=new JButton("Exit");
        exitHandler=new ExitButtonHandler();
        exitButton.addActionListener(exitHandler);
        exitButton.setFont(new Font("Arial",0, 30));

        initButton=new JButton("New Game");
        initHandler=new InitButtonHandler();
        initButton.addActionListener(initHandler);

        //Create result label
        result=new JLabel("O", SwingConstants.CENTER);
        result.setForeground(Color.white);
        result.setFont(new Font("Arial",0, 30));

        t=new JLabel("Current Game: Mobius Strip", SwingConstants.CENTER);
        t.setForeground(Color.white);
        t.setFont(new Font("Arial",0, 30));

        content.add(initButton);
        content.add(result);
        content.add(exitButton);
        content.add(t);

        //Initialize
         */

        int vilmafdocdetpol=0;

        new Thread(thread).start();
        init();
    }

    public void init()
    {
        //Initialize booleans
        O=false;
        gameOver=false;

        //Initialize result label
        //result.setText("X's turn");

        setVisible(true);
    }

    public void update(String arg){
        if(arg.equals("$day")){content.setBackground(Color.blue.brighter().brighter());}
        else if(arg.equals("$night")){content.setBackground(Color.blue.darker().darker());}
        else if(arg.substring(0,1).equals("$")){
            if(arg.substring(0,2).equals("$v")){
                text.setText("Vote to execute:");
                vilmafdocdetpol=0;}
            if(arg.substring(0,2).equals("$k")){
                text.setText("Choose to kill:");
                vilmafdocdetpol=1;}
            if(arg.substring(0,2).equals("$s")){
                text.setText("Choose to save:");
                vilmafdocdetpol=2;}
            if(arg.substring(0,2).equals("$p")){
                text.setText("Choose to scramble:");
                vilmafdocdetpol=3;}
            if(arg.substring(0,2).equals("$i")){
                text.setText("Choose to investigate:");
                vilmafdocdetpol=4;}
            for(int i=0;i<votes.length;i++){
                if(votes[i]!=null){
                    //Container parent = votes[i].getParent();
                    buttonPanel.remove(votes[i]);
                    buttonPanel.revalidate();
                    buttonPanel.repaint();
                }
            }
            //JButton[] votes = new JButton[100];
            voteHandler[] voters = new voteHandler[100];
            String[] args = arg.split(" ");
            for(int i=1; i<args.length; i++){
                votes[i-1] = new JButton(args[i]);
                voters[i-1]=new voteHandler();
                votes[i-1].addActionListener(voters[i-1]);
                //content.add(votes[i-1], BorderLayout.LINE_START);
                votes[i-1].setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.add(votes[i-1]);
            }
            holder.remove(buttonPanel);
            holder.add(buttonPanel, BorderLayout.CENTER);
            //init();
            //content.update();
        }

        result.setText(result.getText().substring(0,result.getText().length()-7) + "<br>" + arg + "</html>");
    }

    public static void main(String[] args)
    {

        Client gui=new Client();

    }

    private class TextHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(chat.getText().length()>0){
                Client.this.update(chat.getText());
                result.setText(result.getText().substring(0,result.getText().length()-7) + "<br>" + chat.getText() + "</html>");

                Client.this.thread.tellServer(chat.getText());
                chat.setText("");
            }
        }
    }

    private class ExitButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            System.exit(0);
        }
    }

    private class InitButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            init();
        }
    }

    private class voteHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            JButton pressed=(JButton)(e.getSource());
            String text=pressed.getText();
            //result.setText(result.getText().substring(0,result.getText().length()-7) + "<br>" + vilmafdocdetpol +text + "</html>");
            if(vilmafdocdetpol==0){thread.tellServer("$vote "+ text);}
            if(vilmafdocdetpol==1){thread.tellServer("$kill "+ text);}
            if(vilmafdocdetpol==2){thread.tellServer("$save "+ text);}
            if(vilmafdocdetpol==3){thread.tellServer("$scramble "+ text);}
            if(vilmafdocdetpol==4){thread.tellServer("$investigate "+ text);}
        }
    }
}
