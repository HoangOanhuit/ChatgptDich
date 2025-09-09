
package com.mycompany.quanlyquanan.view.Reports;

import com.formdev.flatlaf.util.Animator;
import com.mycompany.quanlyquanan.model.ModelChart;
import com.mycompany.quanlyquanan.model.ModelLegend;
import com.mycompany.quanlyquanan.view.Chart.BlankPlotChart;
import com.mycompany.quanlyquanan.view.Chart.BlankPlotChatRender;
import com.mycompany.quanlyquanan.view.Chart.SeriesSize;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;


public class ChartPanel extends javax.swing.JPanel {

    private List<ModelLegend> legends = new ArrayList<>();
    private List<ModelChart> modelChart = new ArrayList<>();
    private final int seriesSize = 18;
    private final int seriesSpace = 10;
//    private final Animator animator;
    private String showLabelString;
    private float animate;
    private Point labelLocation = new Point();
    
    
    public ChartPanel() {
        initComponents();
        initData();
        blankPlotChart1.setBlankPlotChatRender(new BlankPlotChatRender(){
            @Override
            public String getLabelText(int index) {
                return modelChart.get(index).getLabel();
                
            }

            @Override
            public void renderSeries(BlankPlotChart chart, Graphics2D g2, SeriesSize size, int index) {
                double totalSeriesWidth = (seriesSize * legends.size()) + (seriesSpace * (legends.size()-1));
                double x = (size.getWidth() - totalSeriesWidth) /2;
                for (int i = 0;i <legends.size();i++){
                    ModelLegend legend = legends.get(i);
                    g2.setColor(legend.getColor());
                    double seriesValues = chart.getSeriesValuesOf(modelChart.get(index).getValues()[i], size.getHeight());
                    g2.fillRect((int)(size.getX() + x), (int)(size.getY() + size.getHeight() - seriesValues), seriesSize, (int)seriesValues);
                    x += seriesSpace + seriesSize;
                }
            }

            @Override
            public void renderSeries(BlankPlotChart chart, Graphics2D g2, SeriesSize size, int index, List<Path2D.Double> gra) {
            }

            @Override
            public boolean mouseMoving(BlankPlotChart chart, MouseEvent evt, Graphics2D g2, SeriesSize size, int index) {
            return true;
            }

            @Override
            public void renderGraphics(Graphics2D g2, List<Path2D.Double> gra) {
            }

            @Override
            public int getMaxLegend() {
                return legends.size();
            }


        });
    }

    public void initData(){
     initLegendData();
//     initChartData();
        
    }
    
    public void initLegendData(){
        this.addLegend("Doanh thu", new Color(189,135,245));
        this.addLegend("Chi phí", new Color(135,189,245));
        this.addLegend("Lợi nhuận", new Color(245,189,135));


        
    }

//    [TODO] cần lấy dữ liệu từ database và lấy 6 tháng gần nhất
//    public void initChartData(){
//       this.addChartData(new ModelChart("January",new double[]{500,200,80}));
//       this.addChartData(new ModelChart("February",new double[]{600,200,80}));
//       this.addChartData(new ModelChart("March",new double[]{500,600,100}));
//       this.addChartData(new ModelChart("April",new double[]{300,300,80}));
//       this.addChartData(new ModelChart("May",new double[]{500,200,60}));
//       this.addChartData(new ModelChart("June",new double[]{1000,200,30}));
//
//    }
    
    public void addLegend(String name, Color color){
        ModelLegend data = new ModelLegend(name, color);
        legends.add(data);
        panelLegend.add(new ChartLegendItem(data));
        panelLegend.repaint();
        panelLegend.revalidate();
    }
    
    public void addChartData(ModelChart data){
        modelChart.add(data);
        blankPlotChart1.setLabelCount(modelChart.size());
        double max = data.getMaxValues();
        if(max > blankPlotChart1.getMaxValues()){
            blankPlotChart1.setMaxValues(max);
        }
    }

    public void clearChartData(){
        modelChart.clear();
        blankPlotChart1.setLabelCount(0);
        blankPlotChart1.setMaxValues(0);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        blankPlotChart1 = new com.mycompany.quanlyquanan.view.Chart.BlankPlotChart();
        panelLegend = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        panelLegend.setOpaque(false);
        panelLegend.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLegend, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
            .addComponent(blankPlotChart1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(blankPlotChart1, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelLegend, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mycompany.quanlyquanan.view.Chart.BlankPlotChart blankPlotChart1;
    private javax.swing.JPanel panelLegend;
    // End of variables declaration//GEN-END:variables
}
