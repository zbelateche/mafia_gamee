import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

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
    private JTextArea result;
    private JLabel t;
    private JButton[] cells;
    private JButton[] votes = new JButton[100];
    private voteHandler[] voters;
    private JPanel buttonPanel;
    //private JButton initButton;
    private JLabel title;
    private JTextField chat;
    private JTextField will;
    private JPanel all;
    private JLabel text;
    private JPanel holder;
    private JTextArea story;
    private JPanel panel;
    private JPanel panel1;
    private JScrollPane pane;
    private JScrollPane pane1;
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
        all.setBorder(BorderFactory.createEmptyBorder(10,30,50,30)); 
        all.setOpaque(false);
        content.add(all);

        try{
            BufferedImage myPicture = ImageIO.read(new File("logo.png"));
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            //Image img = icon.getImage();
            all.add(picLabel, BorderLayout.PAGE_START);
        }catch (IOException e) {
            title=new JLabel("Mafia!", SwingConstants.CENTER);
            title.setForeground(Color.white);
            title.setFont(new Font("Arial",0, 50));
            all.add(title, BorderLayout.PAGE_START);
        }

        result=new JTextArea("_____\nChat:\n");
        //result.setColumns(10);
        result.setLineWrap(true);
        //result.setVerticalAlignment((float)SwingConstants.BOTTOM);
        result.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
        result.setForeground(Color.white);
        result.setBackground(Color.blue.darker().darker());
        result.setFont(new Font("Arial",0, font));
        result.setEditable(false);
        //result.setSize(10,10);
        //result.setMaximumSize(result.getSize());

        story =new JTextArea("______\nStory:\n");
        story.setForeground(Color.red);
        story.setFont(new Font("Arial",50, font));
        story.setBackground(Color.blue.darker().darker());
        //story.setColumns(30);
        story.setLineWrap(true);
        story.setEditable(false);
        //story.setVerticalAlignment(JLabel.BOTTOM);

        panel = new JPanel( new BorderLayout() );
        panel.setBackground( result.getBackground() );
        panel.setBorder( result.getBorder() );
        result.setBorder( null );
        panel.add(result, BorderLayout.SOUTH);
        //add(new JScrollPane(panel));
        pane = new JScrollPane();
        pane.getViewport().setBackground(result.getBackground());
        pane.getViewport().add(panel);
        pane.setBorder(BorderFactory.createEmptyBorder());
        //pane.setBackground(Color.red.darker().darker());
        pane.setViewportBorder(BorderFactory.createEmptyBorder());
        result.setCaretPosition(result.getDocument().getLength());
        //pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //pane.getVerticalScrollBar().setBackground(result.getBackground());

        panel1 = new JPanel( new BorderLayout() );
        panel1.setBackground( story.getBackground() );
        panel1.setBorder( story.getBorder() );
        story.setBorder( null );
        panel1.add(story, BorderLayout.SOUTH);

        pane1 = new JScrollPane();
        pane1.getViewport().setBackground(result.getBackground());
        pane1.getViewport().add(panel1);
        story.setCaretPosition(story.getDocument().getLength());
        pane1.setBorder(BorderFactory.createEmptyBorder());
        pane1.setViewportBorder(BorderFactory.createEmptyBorder());
        //pane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel sidebyside = new JPanel();
        sidebyside.setLayout(new GridLayout(1,2));
        sidebyside.add(pane);
        sidebyside.add(pane1);
        sidebyside.setOpaque(false);
        //result.setSize(result.getSize().height,pane.getSize().width);

        all.add(sidebyside, BorderLayout.CENTER);
        //result.setColumns(pane.getViewport().getSize().width);
        //all.add(story, BorderLayout.LINE_END);
        //all.add(result, BorderLayout.CENTER);

        chat=new JTextField("Type to chat", 2);
        textHandler=new TextHandler();
        chat.addActionListener(textHandler);
        chat.setFont(new Font("Arial",0, font));
        all.add(chat, BorderLayout.PAGE_END);

        holder = new JPanel();
        BorderLayout borl = new BorderLayout();
        borl.setVgap(15);
        holder.setLayout(borl);
        holder.setBackground(Color.red.darker().darker());
        Border loweredetched = BorderFactory.createRaisedBevelBorder();
        holder.setBorder(BorderFactory.createCompoundBorder(loweredetched,BorderFactory.createEmptyBorder(20,10,20,10)));

        text = new JLabel("Vote to Execute:");
        text.setForeground(Color.white);
        text.setFont(new Font("Arial",50, font));
        holder.add(text, BorderLayout.PAGE_START);
        buttonPanel = new JPanel();
        //buttonPanel.setBackground(Color.red.darker().darker());
        buttonPanel.setOpaque(false);

        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
        holder.add(buttonPanel, BorderLayout.CENTER);
        //holder.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        //JLabel wt = new JLabel("Your Will:");
        //wt.setForeground(Color.white);
        //wt.setFont(new Font("Arial",0, font));
        will = new JTextField("Enter your will");
        will.setFont(new Font("Arial",0, font));
        will.addActionListener(new WillHandler());
        //holder.add(wt, BorderLayout.PAGE_END);
        holder.add(will, BorderLayout.PAGE_END);
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
        if(arg.equals("$day")){content.setBackground(new Color(135,206,250));
            result.setForeground(Color.black);
            story.setForeground(Color.red.darker());
            holder.setBackground(new Color(190,0,0));
            text.setForeground(Color.black);
            story.setBackground(new Color(135,206,250));
            result.setBackground(new Color(135,206,250));
            panel.setBackground( result.getBackground() );
            panel1.setBackground( result.getBackground() );
            pane.getViewport().setBackground(result.getBackground());
            pane1.getViewport().setBackground(result.getBackground());
        }
        else if(arg.equals("$night")){content.setBackground(Color.blue.darker().darker());
            result.setForeground(Color.white);
            story.setForeground(Color.red);
            holder.setBackground(Color.red.darker().darker());
            text.setForeground(Color.white);
            result.setBackground(Color.blue.darker().darker());
            story.setBackground(Color.blue.darker().darker());
            panel.setBackground( result.getBackground() );
            panel1.setBackground( result.getBackground() );
            pane.getViewport().setBackground(result.getBackground());
            pane1.getViewport().setBackground(result.getBackground());
        }
        if(arg.length()>=2){
            if(arg.substring(0,2).equals("$a")){
                text.setText("<html>The Mafia have<br>recruited you.</html>");
                vilmafdocdetpol=6;
                for(int i=0;i<votes.length;i++){
                    if(votes[i]!=null){
                        //Container parent = votes[i].getParent();
                        buttonPanel.remove(votes[i]);
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                    }
                }
                voteHandler[] voters = new voteHandler[100];
                votes[0] = new JButton("Accept");
                votes[1] = new JButton("Deny");
                voters[0]=new voteHandler();
                votes[0].addActionListener(voters[0]);
                //content.add(votes[i-1], BorderLayout.LINE_START);
                votes[0].setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.add(votes[0]);
                voters[1]=new voteHandler();
                votes[1].addActionListener(voters[1]);
                //content.add(votes[i-1], BorderLayout.LINE_START);
                votes[1].setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPanel.add(votes[1]);
            }
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
                if(arg.substring(0,2).equals("$r")){
                    text.setText("Choose to recruit:");
                    vilmafdocdetpol=5;}
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
                    votes[i-1] = new JButton(args[i]){
                        {
                            setSize(100, 30);
                            setMaximumSize(getSize());
                        }
                    };
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
            else if(arg.substring(0,1).equals("@")){
                story.append(arg.substring(1,arg.length())+"\n");
            }
            else{result.append(arg+"\n");}
        }
        if(arg.equals(" ")){
            result.append("\n");
        }
        story.setCaretPosition(story.getDocument().getLength());
        result.setCaretPosition(result.getDocument().getLength());
        //result.setSize(result.getSize().height,pane.getSize().width);
        //story.setSize(story.getSize().height,pane1.getSize().width);
    }

    public static void main(String[] args)
    {
        try{UIManager.setLookAndFeel(
                "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");}
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
        Client gui=new Client();

    }

    private class TextHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(chat.getText().length()>0){
                //Client.this.update(chat.getText());
                result.append(chat.getText()+"\n");

                //result.setSize(result.getSize().height,pane.getSize().width);
                //story.setSize(story.getSize().height,pane1.getSize().width);

                Client.this.thread.tellServer(chat.getText());
                if(chat.getText().toLowerCase().equals("exit")){System.exit(0);}
                chat.setText("");
            }
        }
    }

    private class WillHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(will.getText().length()>0){
                //Client.this.update(chat.getText());
                story.append("New Will: "+will.getText()+"\n");

                Client.this.thread.tellServer("$changewill "+will.getText());

                will.setText("Enter your will");
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
            if(vilmafdocdetpol==5){thread.tellServer("$recruit "+ text);}
            if(vilmafdocdetpol==6){thread.tellServer("$aord "+ text);}
            for(int i=0;i<votes.length;i++){
                if(votes[i]!=null){
                    //Container parent = votes[i].getParent();
                    buttonPanel.remove(votes[i]);
                    buttonPanel.revalidate();
                    buttonPanel.repaint();
                }
            }
        }
    }
}
