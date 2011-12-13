package org.gwtmpv.widgets.form;

import static org.gwtmpv.model.properties.NewProperty.stringProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import joist.util.Join;

import org.gwtmpv.model.properties.StringProperty;
import org.gwtmpv.model.validation.Valid;
import org.gwtmpv.util.HTMLPanelBuilder;
import org.gwtmpv.widgets.StubTextBox;
import org.gwtmpv.widgets.form.lines.StaticFormLine;
import org.gwtmpv.widgets.form.lines.TextBoxFormLine;
import org.junit.Test;

public class FormPresenterTest extends AbstractFormPresenterTest {

  final EmployeeModel employee = new EmployeeModel();

  @Test
  public void htmlOfOneTextBox() {
    p.add(new TextBoxFormLine(employee.firstName));

    assertThat(html().getHtml(), is(Join.join(new String[] {//
      "<div class=\"form\">",//
        "<div class=\"lines\"><ol>",
        "<li>",
        "<div class=\"label\"><label for=\"p-firstName\">First Name</label></div>",//
        "<div class=\"value\"><div id=\"mpv-hb-1\"></div><div class=\"errors\"><div id=\"mpv-hb-2\"></div></div></div>",//
        "</li>",
        "</ol></div>",
        "</div>" },
      "")));
  }

  @Test
  public void allValid() {
    final StringProperty name = stringProperty("name").max(10);
    p.add(new TextBoxFormLine(name));
    name.set("0123456789a");
    assertThat(p.allValid().wasValid(), is(Valid.NO));

    name.set("1");
    assertThat(p.allValid().wasValid(), is(Valid.YES));
  }

  @Test
  public void focusFirstLine() {
    TextBoxFormLine l = new TextBoxFormLine(employee.firstName);
    p.add(l);
    p.focusFirstLine();
    assertThat(((StubTextBox) l.getTextBox()).isFocused(), is(true));
  }

  @Test
  public void customLine() {
    p.add(new TextBoxFormLine(employee.firstName));
    p.add(new StaticFormLine() {
      @Override
      public void render(FormPresenter p, FormLayout l, HTMLPanelBuilder hb) {
        hb.add("some custom html");
      }
    });

    assertThat(html().getHtml(), is(Join.join(new String[] {//
      "<div class=\"form\">",//
        "<div class=\"lines\"><ol>",
        "<li>",
        "<div class=\"label\"><label for=\"p-firstName\">First Name</label></div>",//
        "<div class=\"value\"><div id=\"mpv-hb-3\"></div><div class=\"errors\"><div id=\"mpv-hb-4\"></div></div></div>",//
        "</li>some custom html",
        "</ol></div>",
        "</div>" },
      "")));
  }
}
