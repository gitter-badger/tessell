package org.tessell.widgets;

import java.util.Iterator;

import org.tessell.gwt.user.client.ui.IsWidget;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * An extension of {@link CompositeIsWidget} to use if you want
 * to have child widgets in an {@code ui.xml} file.
 *
 * Specifically, something like:
 * 
 * <my:Foo>
 *   <gwt:FlowPanel />
 * </my:Foo>
 */
public abstract class CompositeHasIsWidgets extends CompositeIsWidget implements HasWidgets {

  public abstract void addIsWidget(IsWidget w);

  @Override
  public void add(Widget w) {
    // by virtue of using Tessell, we assume this is an IsWidget
    addIsWidget((IsWidget) w);
  }

  public void add(IsStubWidget w) {
    addIsWidget((IsWidget) w);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Not expected to be called by UiBinder");
  }

  @Override
  public Iterator<Widget> iterator() {
    throw new UnsupportedOperationException("Not expected to be called by UiBinder");
  }

  @Override
  public boolean remove(Widget w) {
    throw new UnsupportedOperationException("Not expected to be called by UiBinder");
  }

}
