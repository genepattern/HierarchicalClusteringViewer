package org.genepattern.plot;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JMenuBar;

/**
 *
 * @author Joshua Gould
 *
 * @param <T>
 * @param <C>
 */
public interface DataSource<T, C extends Component> {
    /**
     * Sets the data for this data source to display.
     *
     * @param obj
     *                The object.
     */
    public void setData(T obj);

    /**
     * Gets the name of this data source.
     *
     * @return The name.
     */
    public String getName();

    /**
     * Gets the component that displays this data source.
     *
     * @return The component.
     */
    public C getComponent();

    /**
     * Gets the menu bar for this data source.
     *
     * @param parentComponent
     *                The parent.
     *
     * @return The menu bar.
     */
    public JMenuBar getMenuBar(Container parentComponent);

}
