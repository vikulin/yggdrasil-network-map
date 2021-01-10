package demo.more;

import org.zkoss.chart.Charts;
import org.zkoss.chart.Series;
import org.zkoss.chart.plotOptions.DataLabels;
import org.zkoss.chart.plotOptions.NetworkGraphLayoutAlgorithm;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class NetworkGraphComposer extends SelectorComposer<Window> {
    @Wire
    Charts chart;

    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        chart.setType(Charts.NETWOKRGRAPH);
        chart.setModel(NetworkGraphData.getModel());
        chart.setHeight("100%");

        NetworkGraphLayoutAlgorithm npla = chart.getPlotOptions().getNetworkGraph().getLayoutAlgorithm();
        npla.setEnableSimulation(true);
        npla.setFriction(-0.9);

        Series series = chart.getSeries();
        DataLabels dl = series.getDataLabels();
        dl.setEnabled(true);
        dl.setLinkFormat("");
        series.setId("lang-tree");
    }
}