package org.genepattern.plot;

import java.awt.Component;

import javax.swing.JComponent;

public interface DataSource2<T, C extends Component, J extends JComponent> extends DataSource<T, C> {

    public J getJComponent();

}