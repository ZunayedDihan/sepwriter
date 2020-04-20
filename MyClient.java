import java.util.ArrayList;
import java.rmi.*;  
import java.io.FileWriter;
import java.io.IOException;
// For GUI
import java.applet.*;
import java.awt.*;
import javax.swing.*; 
import java.awt.event.*;
//For Status
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyClient{  
	public static void main(String args[]){
		new GUI();
	}  
}

class GUI extends JFrame implements ActionListener {
	public static int connected=0;
	JFrame f=new JFrame();
	JLabel l1=new JLabel("Location of your logcat file: ");
	JLabel l2=new JLabel("Location of your Device Tree: ");
	JLabel l3=new JLabel("Your denials are located: ");	
	public static JLabel status = new JLabel("Device Status: ", JLabel.CENTER);
	JTextField t1=new JTextField(20);
	JTextField t2=new JTextField(20);
	JTextField t3=new JTextField(20);
	JButton b1=new JButton("Get Denials");
	JButton b2=new JButton("Get Logs");
	JButton b3=new JButton("Browse");
	JButton b4=new JButton("Browse");
	JPanel p1 = new JPanel(new GridLayout(3, 2)); 
	JPanel p2 = new JPanel();
	JPanel p3 = new JPanel();
	JFileChooser fc = new JFileChooser();
	GUI(){
		p2.setLayout(new FlowLayout());
		p3.setLayout(new FlowLayout());
		p1.add(l1);
		p1.add(t1);
		p1.add(b3);
		p1.add(l2);		
		p1.add(t2);
		p1.add(b4);
		p1.add(l3);		
		p1.add(t3);
		p2.add(b1);
		p2.add(b2);
		p3.add(status);
		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);
		b4.addActionListener(this);
		t3.setEditable(false); 
		f.add(p1, "North"); 
		f.add(p2);
		f.add(p3, "South");
		f.setVisible(true);
		f.setSize(400,180);
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(GUI::run, 0, 1, TimeUnit.SECONDS);
	}

	private static void run() {
		int count=0;
		try {
			Process process = Runtime.getRuntime().exec("adb devices");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			line = reader.readLine();
			while ((line = reader.readLine()) != null) {
 				if(line.contains("device"))
					count++;			
			}
		} catch (IOException e) { System.out.println(e); }
		if(count==0){
			status.setText("Device Status: Disconnected");
			connected=0;
			status.setForeground(Color.red);
		}else if(count==1){
			status.setText("Device Status: Connected");
			connected=1;
			status.setForeground(Color.green);
		}else if(count>1){
			status.setText("Device Status: " + count + " devices Connected");
			connected=0;			
			status.setForeground(Color.red);
		}
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==b1){
			String s1 = t1.getText();
			if(s1.contains("\\"))
			s1 = s1.replaceAll("\\\\", "/");
			String fname = s1.substring(0,s1.lastIndexOf("/")) + "/Rules.txt";
			try{  
				FileWriter writer = new FileWriter(fname);
				Sepolicy resolver=(Sepolicy)Naming.lookup("rmi://localhost:5000/jp_rmi");  
				ArrayList<String> denials = resolver.getDenials(s1);
				ArrayList<String> rules = resolver.getRules(denials);	  
				for( String rule : rules){
					System.out.println("in " + rule.split(" ", 0)[1] + ".te\n" + rule);
					writer.write("in " + rule.split(" ", 0)[1] + ".te\n" + rule + "\n");
				}
				writer.close();
				}catch(Exception ex){}				
			t2.setText("Stored at " + fname);
			Runtime runtime = Runtime.getRuntime();
			try{    
				runtime.exec("notepad.exe " + fname);
			}catch(IOException ex){}
		}else if(e.getSource()==b3){
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showOpenDialog(GUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				t1.setText(fc.getSelectedFile().getPath());
			}
		}else if(e.getSource()==b4){
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(GUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				t2.setText(fc.getSelectedFile().getPath());
			}
		}
	}
}