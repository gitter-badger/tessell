package org.gwtmpv.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StubHTMLPanel extends StubComplexPanel implements IsHTMLPanel {

  private final String html;
  private final Map<String, IsWidget> replaced = new HashMap<String, IsWidget>();

  public StubHTMLPanel() {
    html = null;
  }

  public StubHTMLPanel(String html) {
    this.html = html;
  }

  @Override
  public void add(IsWidget widget, IsElement elem) {
    super.add(widget);
  }

  public String getHtml() {
    return html;
  }

  @Override
  public void addAndReplaceElement(IsWidget widget, IsElement elem) {
    super.add(widget);
  }

  @Override
  public void addAndReplaceElement(IsWidget widget, String id) {
    super.add(widget);
    replaced.put(id, widget);
  }

  public IsWidget getElementById(String id) {
    for (Iterator<IsWidget> i = iteratorIsWidgets(); i.hasNext();) {
      IsWidget w = i.next();
      if (id.equals(w.getIsElement().getId())) {
        return w;
      }
    }
    throw new IllegalArgumentException("Element not found with id " + id);
  }

  public IsWidget getReplaced(String id) {
    IsWidget w = replaced.get(id);
    if (w == null) {
      throw new IllegalArgumentException("Could not find " + id);
    }
    return w;
  }

}
