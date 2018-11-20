package com.nugsky.tugasakhir.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.util.List;

public class CorrelationDisplay extends ApplicationFrame {
    /**
     * Constructs a new application frame.
     *
     * @param title the frame title.
     */
    public CorrelationDisplay(String title, List<Double> data) {
        super(title);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JFreeChart lineChart = ChartFactory.createLineChart(
                title,
                "Frame","Correlation",
                createDataset(data),
                PlotOrientation.VERTICAL,
                true,true,false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        setContentPane( chartPanel );
    }

    private CategoryDataset createDataset(List<Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for(int i=0;i<data.size();i++){
            dataset.addValue(data.get(i),"correlation value",new Integer(i));
        }
        return dataset;
    }

    @Override
    public void windowClosing(WindowEvent event) {
        if (event.getWindow() == this) {
            dispose();
        }
    }
}
