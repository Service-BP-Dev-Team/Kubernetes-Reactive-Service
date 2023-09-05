package com.reactive.service.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

public class UIUtil {

	public static JPanel[][] layout(int i, int j, JComponent comp){
		comp.removeAll();
		JPanel[][] mypanes =new JPanel[i][j];
		JComponent content = comp;
		int lenght = i*50;
		if(content.getPreferredSize().getHeight()<lenght) {
			//JScrollPane scPane= new JScrollPane();
			//scPane.setPreferredSize(new Dimension((int)content.getSize().getWidth(),lenght));
			content.setLayout(new BorderLayout());
			
			JPanel newContent =new JPanel();
			newContent.setPreferredSize(new Dimension((int)content.getPreferredSize().getWidth(),lenght));
			content.add(new JScrollPane(newContent));
			content=newContent;
		}
		content.setBorder(new LineBorder(new Color(0, 0, 0)));
		content.setLayout(new GridLayout(i, j,10,10));
		for(int m=0;m<mypanes.length;m++) {
			for(int n=0;n<mypanes[m].length;n++) {
				mypanes[m][n] = new JPanel();
				mypanes[m][n].setLayout(new GridBagLayout());
				//mypanes[m][n].setBorder(new LineBorder(new Color(0, 0, 0)));
				content.add(mypanes[m][n]);
			}
		}
		return mypanes;
	}
}
