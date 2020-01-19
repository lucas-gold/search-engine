package CPS842;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class GUI {



	//Imports are listed in full to show what's being used
	//could just import javax.swing.* and java.awt.* etc..


	public static void main(String[] args) throws FileNotFoundException {

		//	
		//	System.out.println("Enter Query ");
		//	Scanner in = new Scanner(System.in);
		//	String query = in.nextLine();
		//	
		//
		//	invert.build(query);
		new GUI();
	}
	public GUI()
	{
		Invert invert = new Invert(); 
		try {
			invert.parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JFrame guiFrame = new JFrame();
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("Technology Search Engine");
		guiFrame.setSize(1200,700);
		guiFrame.setLocationRelativeTo(null);

		JPanel comboPanel = new JPanel();
		JPanel resultPanel = new JPanel();

		JLabel searchLbl = new JLabel("Search:");
		JTextField query = new JTextField(48);
		JButton submit = new JButton("Submit");
		JTextArea result = new JTextArea(38, 88);
		result.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(result); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		result.setEditable(false);

		submit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				String submit = event.getActionCommand(); 
				if (submit.equals("Submit")) { 
					String s = query.getText();
					String post = invert.build(s);
					result.setText(post);
				} 
			}
		});
		comboPanel.add(searchLbl);
		comboPanel.add(query);
		comboPanel.add(submit);
		resultPanel.add(scrollPane);

		guiFrame.add(comboPanel, BorderLayout.NORTH);
		guiFrame.add(resultPanel,BorderLayout.CENTER);

		guiFrame.setVisible(true);
	}
}

