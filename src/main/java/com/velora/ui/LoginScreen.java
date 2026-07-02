package com.velora.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Real component-based responsive Velora Motors login. */
public final class LoginScreen extends JFrame {
    public LoginScreen() {
        super("Velora Motors — Manager Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(1120, 700));
        setContentPane(new LuxuryView(this));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    void toggleMaximize() {
        int s=getExtendedState();
        setExtendedState((s&Frame.MAXIMIZED_BOTH)!=0?Frame.NORMAL:Frame.MAXIMIZED_BOTH);
    }

    private static final class LuxuryView extends BackgroundPane {
        static final Color GOLD=new Color(214,168,91), PALE=new Color(235,202,156);
        static final Color WHITE=new Color(248,248,248), MUTED=new Color(174,179,188);
        private final LoginScreen frame;
        private final List<Animated> animations=new ArrayList<>();
        private final long started=System.nanoTime();
        private final Stats stats=new Stats();
        private float intro;

        LuxuryView(LoginScreen frame) {
            super(load("/images/velora-dealership.png"));
            this.frame=frame;
            setLayout(new PhysicalViewportLayout());
            JPanel viewport=transparent(new JPanel(new BorderLayout()));
            viewport.add(buildMain(),BorderLayout.CENTER);
            viewport.add(buildFooter(),BorderLayout.SOUTH);
            add(viewport);
            javax.swing.Timer timer=new javax.swing.Timer(16,e->{
                intro=ease(Math.min(1f,(System.nanoTime()-started)/700_000_000f));
                for(Animated a:animations)a.tick();
                stats.progress=ease(Math.min(1f,(System.nanoTime()-started)/1_400_000_000f));
                repaint();
            });
            timer.setCoalesce(true);timer.start();
        }

        private JComponent buildMain() {
            JPanel main=transparent(new JPanel(new GridBagLayout()));
            GridBagConstraints c=fill();
            c.gridx=0;c.weightx=.66;main.add(buildHero(),c);
            c.gridx=1;c.weightx=.34;main.add(buildRight(),c);
            return main;
        }

        private JComponent buildHero() {
            JPanel hero=transparent(new JPanel(new BorderLayout(16,16)));
            hero.setBorder(new EmptyBorder(24,40,16,20));
            hero.add(buildHeroTop(),BorderLayout.NORTH);
            JPanel center=transparent(new JPanel(new GridBagLayout()));
            GridBagConstraints c=fill();c.anchor=GridBagConstraints.NORTH;c.fill=GridBagConstraints.HORIZONTAL;
            c.weighty=1;c.insets=new Insets(20,210,0,170);
            GlassPanel sign=new GlassPanel(14,112);sign.setLayout(new FlowLayout(FlowLayout.CENTER,12,9));
            sign.add(new BmwLogo(34));sign.add(text("VELORA MOTORS",20,Font.PLAIN,WHITE));
            center.add(sign,c);hero.add(center,BorderLayout.CENTER);
            hero.add(buildHeroBottom(),BorderLayout.SOUTH);
            return hero;
        }

        private JComponent buildHeroTop() {
            JPanel top=transparent(new JPanel(new GridBagLayout()));
            GridBagConstraints c=fill();c.gridy=0;
            Brand brand=new Brand();c.gridx=0;c.weightx=.59;top.add(brand,c);
            GlassPanel trust=new GlassPanel(16,120);trust.setLayout(new GridLayout(1,4));
            trust.setPreferredSize(new Dimension(440,136));
            trust.setMinimumSize(new Dimension(0,0));
            String[][] data={{"shield.svg","SECURE SYSTEM","Your data is always","protected with us."},
                {"calendar.svg","24/7 SUPPORT","We're here for you,","anytime, anywhere."},
                {"star.svg","PREMIUM EXPERIENCE","Excellence in every","drive, every time."},
                {"tag.svg","LOYALTY REWARDS","Earn points & unlock","exclusive benefits."}};
            for(String[] d:data){
                JPanel p=transparent(new JPanel());p.setBorder(new EmptyBorder(12,4,8,4));p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
                p.setMinimumSize(new Dimension(0,0));
                JLabel icon=new JLabel(new SvgIcon(d[0],25,GOLD));icon.setAlignmentX(.5f);p.add(icon);p.add(Box.createVerticalStrut(8));
                for(int i=1;i<4;i++){JLabel l=text(d[i],i==1?8:9,i==1?Font.BOLD:Font.PLAIN,i==1?PALE:MUTED);l.setAlignmentX(.5f);p.add(l);p.add(Box.createVerticalStrut(4));}
                trust.add(p);
            }
            c.gridx=1;c.weightx=.41;c.insets=new Insets(34,0,54,0);top.add(trust,c);
            return top;
        }

        private JComponent buildHeroBottom() {
            JPanel bottom=transparent(new JPanel());bottom.setLayout(new BoxLayout(bottom,BoxLayout.Y_AXIS));
            GlassPanel features=new GlassPanel(18,130);features.setLayout(new GridLayout(1,4));
            features.setPreferredSize(new Dimension(860,132));
            String[][] d={{"car.svg","LATEST BMW MODELS","Drive the newest BMW models","with cutting-edge technology."},
                {"calendar.svg","FLEXIBLE RENTALS","Daily, weekly, or monthly","rentals to fit your schedule."},
                {"shield.svg","FULL INSURANCE","All rentals include comprehensive","insurance coverage."},
                {"tag.svg","BEST RATES","Competitive pricing with","premium service."}};
            for(String[] x:d){Feature f=new Feature(x);animations.add(f);features.add(f);}
            bottom.add(features);bottom.add(Box.createVerticalStrut(10));
            JPanel row=transparent(new JPanel(new GridBagLayout()));
            Quote quote=new Quote();animations.add(quote);
            GridBagConstraints c=fill();c.gridx=0;c.weightx=.42;row.add(quote,c);
            c.gridx=1;c.weightx=.58;row.add(stats,c);
            row.setPreferredSize(new Dimension(860,80));bottom.add(row);
            return bottom;
        }

        private JComponent buildRight() {
            JPanel right=transparent(new JPanel());right.setBorder(new EmptyBorder(0,10,16,24));
            right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));
            right.add(windowControls());right.add(Box.createVerticalStrut(2));
            LoginCard login=new LoginCard();login.setAlignmentX(.5f);right.add(login);
            right.add(Box.createVerticalStrut(10));
            Featured featured=new Featured();featured.setAlignmentX(.5f);right.add(featured);
            return right;
        }

        private JComponent windowControls() {
            JPanel p=transparent(new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
            String[] symbols={"—","□","×"};
            for(int i=0;i<3;i++){final int n=i;JButton b=ghost(symbols[i]);b.setPreferredSize(new Dimension(44,30));
                b.addActionListener(e->{if(n==0)frame.setState(Frame.ICONIFIED);else if(n==1)frame.toggleMaximize();else frame.dispose();});p.add(b);}
            return p;
        }

        private JComponent buildFooter() {
            JPanel footer=new JPanel(new GridBagLayout()){
                protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setColor(new Color(3,7,12,242));g.fillRect(0,0,getWidth(),getHeight());
                    g.setColor(new Color(255,215,150,28));g.drawLine(0,0,getWidth(),0);g.dispose();}
            };
            footer.setOpaque(false);footer.setBorder(new EmptyBorder(10,38,10,38));footer.setPreferredSize(new Dimension(100,68));
            GridBagConstraints c=fill();c.gridy=0;
            JPanel company=transparent(new JPanel());company.setLayout(new BoxLayout(company,BoxLayout.Y_AXIS));
            company.add(text("◆   V E L O R A   M O T O R S",14,Font.PLAIN,new Color(207,207,211)));
            company.add(text("      Premium BMW Vehicle Rental System",11,Font.PLAIN,MUTED));
            c.gridx=0;c.weightx=.35;footer.add(company,c);
            JLabel copy=text("© 2026 Velora Motors. All rights reserved.",13,Font.PLAIN,MUTED);copy.setHorizontalAlignment(SwingConstants.CENTER);
            c.gridx=1;c.weightx=.30;footer.add(copy,c);
            JPanel social=transparent(new JPanel(new FlowLayout(FlowLayout.RIGHT,14,0)));social.add(text("F O L L O W   U S",11,Font.PLAIN,MUTED));
            for(String s:new String[]{"f","◎","in","▶"}){Social b=new Social(s);animations.add(b);social.add(b);}
            c.gridx=2;c.weightx=.35;footer.add(social,c);return footer;
        }

        private final class LoginCard extends GlassPanel {
            private final PromptField user=new PromptField("Enter your username",new SvgIcon("user.svg",20,MUTED),false);
            private final PromptField pass=new PromptField("Enter your password",new SvgIcon("lock.svg",20,MUTED),true);
            private final GoldButton sign=new GoldButton("SIGN IN     →");
            private int failed,lock;
            LoginCard(){
                super(28,180);setBorder(new EmptyBorder(18,38,18,38));setLayout(new GridBagLayout());
                setPreferredSize(new Dimension(510,530));setMinimumSize(new Dimension(0,500));setMaximumSize(new Dimension(Integer.MAX_VALUE,550));
                GridBagConstraints c=fill();c.gridx=0;c.weightx=1;c.insets=new Insets(0,0,5,0);
                c.gridy=0;c.weighty=.12;add(new BmwLogo(58),c);
                c.gridy++;c.weighty=.055;add(center("M A N A G E R   L O G I N",21,Font.PLAIN,PALE),c);
                c.gridy++;c.weighty=.04;add(center("Welcome back! Please sign in to continue.",13,Font.PLAIN,WHITE),c);
                c.gridy++;c.weighty=.035;add(new Divider(),c);
                c.gridy++;c.weighty=.035;add(text("USERNAME",12,Font.PLAIN,WHITE),c);
                c.gridy++;c.weighty=.08;add(user,c);
                c.gridy++;c.weighty=.035;add(text("PASSWORD",12,Font.PLAIN,WHITE),c);
                c.gridy++;c.weighty=.08;add(pass,c);
                JPanel options=transparent(new JPanel(new BorderLayout()));
                JCheckBox remember=new JCheckBox("Remember Me",true);remember.setOpaque(false);remember.setForeground(WHITE);remember.setFont(new Font("Segoe UI",Font.PLAIN,11));remember.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                JButton forgot=ghost("Forgot Password?");forgot.setForeground(PALE);forgot.setFont(new Font("Segoe UI",Font.PLAIN,10));forgot.addActionListener(e->JOptionPane.showMessageDialog(this,"Please contact your administrator."));
                options.add(remember,BorderLayout.WEST);options.add(forgot,BorderLayout.EAST);
                c.gridy++;c.weighty=.055;add(options,c);
                c.gridy++;c.weighty=.085;add(sign,c);
                c.gridy++;c.weighty=.11;add(new Notice(false),c);
                c.gridy++;c.weighty=.12;add(new Notice(true),c);
                animations.add(sign);
                sign.addActionListener(e->authenticate());
                pass.addActionListener(e->authenticate());
            }
            void authenticate(){if(lock>0)return;if("manager".equalsIgnoreCase(user.getText().trim())&&"velora2026".equals(new String(pass.getPassword())))
                JOptionPane.showMessageDialog(this,"Welcome to Velora Motors Management System.");else if(++failed>=3){lock=30;sign.setEnabled(false);}}
        }

        private final class Featured extends GlassPanel {
            Featured(){super(22,155);setLayout(new GridBagLayout());setPreferredSize(new Dimension(510,150));setMinimumSize(new Dimension(0,138));setMaximumSize(new Dimension(Integer.MAX_VALUE,158));
                JPanel copy=transparent(new JPanel());copy.setBorder(new EmptyBorder(19,22,14,8));copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));
                copy.add(text("FEATURED MODEL",9,Font.PLAIN,PALE));copy.add(Box.createVerticalStrut(5));copy.add(text("BMW i8 ROADSTER",16,Font.BOLD,WHITE));
                copy.add(Box.createVerticalStrut(6));copy.add(text("Future meets performance.",10,Font.PLAIN,MUTED));copy.add(text("Rent the extraordinary.",10,Font.PLAIN,MUTED));
                copy.add(Box.createVerticalGlue());JButton b=ghost("EXPLORE NOW   ›");b.setForeground(WHITE);b.setBorder(BorderFactory.createLineBorder(new Color(214,168,91,85)));copy.add(b);
                GridBagConstraints c=fill();c.gridx=0;c.weightx=.48;add(copy,c);c.gridx=1;c.weightx=.52;add(new CarPanel(image),c);}
        }

        private static final class PromptField extends JPasswordField {
            private final String prompt;
            private final Icon leading;
            PromptField(String prompt,Icon leading,boolean password){
                this.prompt=prompt;this.leading=leading;setEchoChar(password?'•':(char)0);
                setOpaque(false);setForeground(WHITE);setCaretColor(GOLD);
                setFont(new Font("Segoe UI",Font.PLAIN,13));
                setBorder(new EmptyBorder(0,48,0,password?42:14));
                setPreferredSize(new Dimension(100,48));setMinimumSize(new Dimension(80,40));
            }
            protected void paintComponent(Graphics raw){
                Graphics2D g=(Graphics2D)raw.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(8,14,20,230));g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g.setColor(new Color(214,168,91,isFocusOwner()?145:38));g.setStroke(new BasicStroke(isFocusOwner()?1.6f:1));g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                leading.paintIcon(this,g,16,(getHeight()-leading.getIconHeight())/2);g.dispose();
                super.paintComponent(raw);
                if(getPassword().length==0){g=(Graphics2D)raw.create();g.setFont(getFont());g.setColor(new Color(135,139,146));
                    g.drawString(prompt,48,(getHeight()+g.getFontMetrics().getAscent())/2-2);g.dispose();}
            }
        }

        private final class Brand extends JComponent {
            Brand(){setPreferredSize(new Dimension(650,245));setMinimumSize(new Dimension(0,0));}
            protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setComposite(AlphaComposite.SrcOver.derive(Math.max(.02f,intro)));int cx=Math.min(getWidth()/2,390);
                double pulse=(Math.sin(System.nanoTime()/1_200_000_000.0)+1)/2;g.setColor(new Color(214,168,91,(int)(16+18*pulse)));g.fillOval(cx-110,5,220,78);
                drawWings(g,cx,30);centerDraw(g,"VELORA MOTORS",cx,132,50,Font.PLAIN,WHITE,"Serif");
                centerDraw(g,"P R E M I U M   B M W   V E H I C L E   R E N T A L   S Y S T E M",cx,174,13,Font.PLAIN,PALE,"SansSerif");
                g.setColor(new Color(214,168,91,150));g.drawLine(cx-265,194,cx-10,194);g.drawLine(cx+10,194,cx+265,194);
                centerDraw(g,"D R I V E   L U X U R Y .   D R I V E   B M W .",cx,225,15,Font.PLAIN,WHITE,"SansSerif");g.dispose();}
        }

        private final class BmwLogo extends JComponent {
            private final int size;BmwLogo(int size){this.size=size;setPreferredSize(new Dimension(size+12,size+12));setMinimumSize(new Dimension(size,size));}
            protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                double a=Math.toRadians(5*Math.sin(System.nanoTime()/4_500_000_000.0*Math.PI));g.rotate(a,getWidth()/2.,getHeight()/2.);
                int r=Math.min(size,Math.min(getWidth(),getHeight()))/2,cx=getWidth()/2,cy=getHeight()/2;g.setColor(WHITE);g.fillOval(cx-r,cy-r,2*r,2*r);g.setColor(new Color(4,8,13));g.fillOval(cx-r+3,cy-r+3,2*r-6,2*r-6);
                int d=2*r-18,x=cx-r+9,y=cy-r+9;g.setColor(WHITE);g.fillArc(x,y,d,d,0,90);g.fillArc(x,y,d,d,180,90);g.setColor(new Color(20,126,199));g.fillArc(x,y,d,d,90,90);g.fillArc(x,y,d,d,270,90);g.dispose();}
        }

        private final class Feature extends HoverPanel {
            Feature(String[] x){super(12);setBorder(new EmptyBorder(18,15,13,13));setLayout(new BorderLayout(12,0));add(new JLabel(new SvgIcon(x[0],27,GOLD)),BorderLayout.WEST);
                JPanel p=transparent(new JPanel());p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(text(x[1],11,Font.BOLD,PALE));p.add(Box.createVerticalStrut(9));p.add(text(x[2],11,Font.PLAIN,WHITE));p.add(text(x[3],11,Font.PLAIN,WHITE));add(p);}
        }
        private final class Quote extends HoverPanel {
            Quote(){super(12);setBorder(new EmptyBorder(13,18,10,18));setLayout(new BorderLayout(10,0));add(text("“",37,Font.BOLD,GOLD),BorderLayout.WEST);
                JPanel p=transparent(new JPanel());p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(text("The Ultimate Driving Experience.",12,Font.ITALIC,WHITE));p.add(text("Now Available For Every Journey.",12,Font.ITALIC,WHITE));add(p);}
        }
        private final class Stats extends GlassPanel {
            float progress;Stats(){super(12,110);}
            protected void paintComponent(Graphics raw){super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();String[] v={(int)(250*progress)+"+",progress>.995?"15K+":String.format("%.1fK+",15*progress),(int)(25*progress)+"+",String.format("%.1f/5",4.9*progress)};
                String[] n={"Premium Vehicles","Satisfied Customers","Locations","Customer Rating"};int cell=getWidth()/4;for(int i=0;i<4;i++){g.setFont(new Font("Segoe UI",Font.BOLD,19));g.setColor(WHITE);drawCenter(g,v[i],i*cell+cell/2,34);
                    g.setFont(new Font("Segoe UI",Font.PLAIN,10));g.setColor(new Color(92,126,180));drawCenter(g,n[i],i*cell+cell/2,58);}g.dispose();}
        }
        private final class Notice extends JComponent {
            final boolean error;Notice(boolean e){error=e;setPreferredSize(new Dimension(100,e?62:54));setMinimumSize(new Dimension(80,e?54:48));}
            protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setColor(error?new Color(75,10,14,210):new Color(7,13,19,215));g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g.setColor(error?new Color(235,65,72):WHITE);g.setFont(new Font("Segoe UI",Font.PLAIN,10));g.drawString(error?"Too many failed attempts.":"For your security, session expires",42,23);g.drawString(error?"Wait 30 seconds before trying again.":"after 10 minutes of inactivity.",42,41);g.dispose();}
        }
        private final class Divider extends JComponent {protected void paintComponent(Graphics g){g.setColor(new Color(214,168,91,170));g.drawLine(getWidth()/2-28,getHeight()/2,getWidth()/2+28,getHeight()/2);}}

        private interface Animated{void tick();}
        private class GlassPanel extends JPanel {final int radius,alpha;GlassPanel(int r,int a){radius=r;alpha=a;setOpaque(false);}
            protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(new Color(5,10,16,alpha));g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);g.setColor(new Color(214,168,91,45));g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);g.dispose();super.paintComponent(raw);}}
        private class HoverPanel extends GlassPanel implements Animated {float hover;HoverPanel(int r){super(r,135);addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}});}
            public void tick(){float t=getMousePosition()==null?0:1;hover+=(t-hover)*.1f;repaint();}protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.translate(0,-Math.round(7*hover));g.setColor(new Color(214,168,91,(int)(30+65*hover)));g.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);g.dispose();super.paintComponent(raw);}}
        private final class GoldButton extends JButton implements Animated {float hover,press;GoldButton(String s){super(s);setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setFont(new Font("Segoe UI",Font.BOLD,14));setForeground(new Color(28,20,12));setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
            public void tick(){hover+=(getModel().isRollover()?1-hover:-hover)*.12f;press+=(getModel().isPressed()?1-press:-press)*.2f;repaint();}
            protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setColor(new Color(214,168,91,(int)(35+70*hover)));g.fillRoundRect(0,3,getWidth(),getHeight()-1,9,9);g.setPaint(new GradientPaint(0,0,new Color(244,207,151),0,getHeight(),new Color(190,137,72)));int in=Math.round(2*press);g.fillRoundRect(in,in,getWidth()-2*in,getHeight()-2*in,8,8);g.dispose();super.paintComponent(raw);}}
        private final class Social extends JButton implements Animated {float hover;Social(String s){super(s);setPreferredSize(new Dimension(34,34));setForeground(PALE);setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
            public void tick(){hover+=(getModel().isRollover()?1-hover:-hover)*.1f;repaint();}protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.rotate(Math.toRadians(8*hover),getWidth()/2.,getHeight()/2.);g.setColor(new Color(214,168,91,(int)(35+55*hover)));g.fillOval(1,1,getWidth()-2,getHeight()-2);g.dispose();super.paintComponent(raw);}}
        private final class CarPanel extends JComponent {final BufferedImage source;CarPanel(BufferedImage i){source=i;}protected void paintComponent(Graphics g){Shape old=g.getClip();g.setClip(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),20,20));g.drawImage(source,0,0,getWidth(),getHeight(),source.getWidth()/5,source.getHeight()/2,source.getWidth()*4/5,source.getHeight(),null);g.setClip(old);}}

        private static JPanel transparent(JPanel p){p.setOpaque(false);return p;}
        private static JLabel text(String s,int z,int style,Color c){JLabel l=new JLabel(s);l.setForeground(c);l.setFont(new Font("Segoe UI",style,z));return l;}
        private static JLabel center(String s,int z,int style,Color c){JLabel l=text(s,z,style,c);l.setHorizontalAlignment(SwingConstants.CENTER);return l;}
        private static JButton ghost(String s){JButton b=new JButton(s);b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setForeground(WHITE);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
        private static GridBagConstraints fill(){GridBagConstraints c=new GridBagConstraints();c.fill=GridBagConstraints.BOTH;c.weighty=1;return c;}
        private static float ease(float p){return 1-(1-p)*(1-p)*(1-p);}
        private static BufferedImage load(String p){try{return ImageIO.read(LuxuryView.class.getResource(p));}catch(IOException|IllegalArgumentException e){throw new IllegalStateException(e);}}
        private static void drawCenter(Graphics2D g,String s,int x,int y){g.drawString(s,x-g.getFontMetrics().stringWidth(s)/2,y);}
        private static void centerDraw(Graphics2D g,String s,int x,int y,int z,int st,Color c,String f){g.setFont(new Font(f,st,z));g.setColor(c);drawCenter(g,s,x,y);}
        private static void drawWings(Graphics2D g,int cx,int y){g.setColor(GOLD);g.setStroke(new BasicStroke(4));Path2D p=new Path2D.Double();p.moveTo(cx,y+55);p.lineTo(cx-37,y);p.lineTo(cx-105,y);p.moveTo(cx,y+55);p.lineTo(cx+37,y);p.lineTo(cx+105,y);p.moveTo(cx-28,y+13);p.lineTo(cx-86,y+13);p.moveTo(cx+28,y+13);p.lineTo(cx+86,y+13);p.moveTo(cx-19,y+26);p.lineTo(cx-63,y+26);p.moveTo(cx+19,y+26);p.lineTo(cx+63,y+26);g.draw(p);}
    }

    private static class BackgroundPane extends JPanel {
        final BufferedImage image;BackgroundPane(BufferedImage i){image=i;}
        protected void paintComponent(Graphics raw){super.paintComponent(raw);double s=Math.max(getWidth()/(double)image.getWidth(),getHeight()/(double)image.getHeight());int w=(int)Math.ceil(image.getWidth()*s),h=(int)Math.ceil(image.getHeight()*s);
            raw.drawImage(image,(getWidth()-w)/2,(getHeight()-h)/2,w,h,null);Graphics2D g=(Graphics2D)raw.create();g.setPaint(new GradientPaint(0,0,new Color(2,6,12,50),getWidth(),0,new Color(1,4,8,130)));g.fillRect(0,0,getWidth(),getHeight());g.dispose();}
    }

    /** Corrects Windows DPI virtualization once; child UI remains layout-managed. */
    private static final class PhysicalViewportLayout implements LayoutManager {
        public void addLayoutComponent(String name,Component component){}
        public void removeLayoutComponent(Component component){}
        public Dimension preferredLayoutSize(Container parent){return parent.getSize();}
        public Dimension minimumLayoutSize(Container parent){return new Dimension(1024,680);}
        public void layoutContainer(Container parent){
            if(parent.getComponentCount()==0)return;
            double dpi=Math.max(1.0,Toolkit.getDefaultToolkit().getScreenResolution()/96.0);
            int width=(int)Math.round(parent.getWidth()/dpi);
            int height=(int)Math.round(parent.getHeight()/dpi);
            parent.getComponent(0).setBounds(0,0,width,height);
        }
    }

    /** Minimal renderer for the project's simple SVG line assets. */
    private static final class SvgIcon implements Icon {
        final int size;final Color color;final List<Shape> shapes=new ArrayList<>();
        SvgIcon(String name,int size,Color color){this.size=size;this.color=color;parse(name);}
        void parse(String name){try(InputStream in=LoginScreen.class.getResourceAsStream("/icons/"+name)){String s=new String(in.readAllBytes(),StandardCharsets.UTF_8);
            Matcher m=Pattern.compile("<(path|polyline)[^>]*points=\"([^\"]+)\"").matcher(s);while(m.find()){Path2D p=new Path2D.Double();String[] a=m.group(2).trim().split("\\s+");for(int i=0;i<a.length;i++){String[] q=a[i].split(",");double x=Double.parseDouble(q[0]),y=Double.parseDouble(q[1]);if(i==0)p.moveTo(x,y);else p.lineTo(x,y);}if(m.group(1).equals("path"))p.closePath();shapes.add(p);}
            m=Pattern.compile("<line x1=\"([^\"]+)\" y1=\"([^\"]+)\" x2=\"([^\"]+)\" y2=\"([^\"]+)\"").matcher(s);while(m.find())shapes.add(new Line2D.Double(d(m,1),d(m,2),d(m,3),d(m,4)));
            m=Pattern.compile("<circle cx=\"([^\"]+)\" cy=\"([^\"]+)\" r=\"([^\"]+)\"").matcher(s);while(m.find())shapes.add(new Ellipse2D.Double(d(m,1)-d(m,3),d(m,2)-d(m,3),d(m,3)*2,d(m,3)*2));
            m=Pattern.compile("<rect x=\"([^\"]+)\" y=\"([^\"]+)\" width=\"([^\"]+)\" height=\"([^\"]+)\"").matcher(s);while(m.find())shapes.add(new RoundRectangle2D.Double(d(m,1),d(m,2),d(m,3),d(m,4),2,2));
        }catch(Exception ignored){}}
        static double d(Matcher m,int i){return Double.parseDouble(m.group(i));}
        public int getIconWidth(){return size;}public int getIconHeight(){return size;}
        public void paintIcon(Component c,Graphics raw,int x,int y){Graphics2D g=(Graphics2D)raw.create();g.translate(x,y);g.scale(size/24.,size/24.);g.setColor(color);g.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));for(Shape s:shapes)g.draw(s);g.dispose();}
    }
}
