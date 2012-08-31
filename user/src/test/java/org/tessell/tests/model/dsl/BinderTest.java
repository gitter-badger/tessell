package org.tessell.tests.model.dsl;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_TAB;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.tessell.model.dsl.TakesValues.textOf;
import static org.tessell.model.dsl.WhenConditions.notNull;
import static org.tessell.model.properties.NewProperty.*;
import static org.tessell.testing.TessellMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tessell.gwt.dom.client.StubClickEvent;
import org.tessell.gwt.dom.client.StubElement;
import org.tessell.gwt.user.client.StubCookies;
import org.tessell.gwt.user.client.ui.*;
import org.tessell.model.dsl.Binder;
import org.tessell.model.properties.*;
import org.tessell.model.validation.Valid;
import org.tessell.model.values.DerivedValue;
import org.tessell.model.values.SetValue;
import org.tessell.tests.model.commands.DummyActiveCommand;
import org.tessell.tests.model.commands.DummyUiCommand;
import org.tessell.tests.model.properties.DummyModel;
import org.tessell.util.cookies.StringCookie;
import org.tessell.widgets.StubTextList;
import org.tessell.widgets.StubWidget;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class BinderTest {

  final Binder binder = new Binder();
  final StringProperty s = stringProperty("s");
  final StubTextBox box = new StubTextBox();
  final StubTextList errors = new StubTextList();

  public static enum Color {
    Blue, Green
  };

  @Test
  public void propertyToWidget() {
    binder.bind(s).to(box);
    s.set("test");
    assertThat(box.getValue(), is("test"));
  }

  @Test
  public void propertyToWidgetIsInitiallyUntouched() {
    binder.bind(s).to(box);
    assertThat(s.isTouched(), is(false));
  }

  @Test
  public void propertyToWidgetImmediatelySetsTheWidgetsValue() {
    s.set("test");
    binder.bind(s).to(box);
    assertThat(box.getValue(), is("test"));
  }

  @Test
  public void propertyToWidgetFiresWidgetChange() {
    binder.bind(s).to(box);
    final boolean[] changed = { false };
    box.addValueChangeHandler(new ValueChangeHandler<String>() {
      public void onValueChange(final ValueChangeEvent<String> event) {
        changed[0] = true;
      }
    });
    s.set("test");
    assertThat(changed[0], is(true));
  }

  @Test
  public void propertyToWidgetCanBeUntouched() {
    final SetValue<String> value = new SetValue<String>("value");
    final StringProperty property = stringProperty(value).req();
    binder.bind(property).to(box);
    // start out with a good value
    property.set("good");
    // now change the value behind the scenes (e.g. a new DTO)
    value.set(null);
    // and untouch the property
    property.setTouched(false);
    // we shouldn't have any errors
    assertThat(property.getErrors().size(), is(0));
  }

  @Test
  public void propertyToListBoxUpdatesListBoxWhenPropertyChanges() {
    final StubListBox listBox = new StubListBox();
    final ArrayList<String> values = new ArrayList<String>();
    values.add(null);
    values.add("a");
    values.add("b");

    binder.bind(s).to(listBox, values);
    s.set("b");
    assertThat(listBox.getSelectedIndex(), is(2));

    s.set(null);
    assertThat(listBox.getSelectedIndex(), is(0));
  }

  @Test
  public void propertyToListBoxUpdatesPropertyWhenListBoxChange() {
    final StubListBox listBox = new StubListBox();
    final ArrayList<String> values = new ArrayList<String>();
    values.add("a");
    values.add("b");

    binder.bind(s).to(listBox, values);
    listBox.select("b");
    assertThat(s.get(), is("b"));
  }

  @Test
  public void propertyToListBoxHandlesNullValue() {
    final StubListBox listBox = new StubListBox();
    final ArrayList<String> values = new ArrayList<String>();
    values.add(null);
    values.add("a");
    values.add("b");

    binder.bind(s).to(listBox, values);
    listBox.select("b");
    assertThat(s.get(), is("b"));

    // null gets converted to "" (otherwise it shows up as "null"), so select that
    listBox.select("");
    assertThat(s.get(), is(nullValue()));
  }

  @Test
  public void propertyToListBoxHandlesEmptyString() {
    final StubListBox listBox = new StubListBox();
    final ArrayList<String> values = new ArrayList<String>();
    values.add("");
    values.add("a");
    values.add("b");

    binder.bind(s).to(listBox, values);
    listBox.select("b");
    assertThat(s.get(), is("b"));

    listBox.select("");
    assertThat(s.get(), is(""));
  }

  @Test
  public void errorsThatAlreadyFiredGetAddedToAnErrorList() {
    s.req().touch();
    assertThat(s.wasValid(), is(Valid.NO));

    binder.bind(s).errorsTo(errors);
    assertThat(errors.getList().size(), is(1));
  }

  @Test
  public void widgetToProperty() {
    binder.bind(s).to(box);
    box.type("test");
    assertThat(s.get(), is("test"));
  }

  @Test
  public void stringPropertyToWidgetSetsMaxLength() {
    s.max(100);
    binder.bind(s).to(box);
    assertThat(box.getMaxLength(), is(100));
  }

  @Test
  public void clickableWidgetToProperty() {
    binder.bind(s).withValue("gotclicked").to(box);
    box.click();
    assertThat(s.get(), is("gotclicked"));
  }

  @Test
  public void whenTrueFiresInitialValueWhenTrue() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).set("c").on(w);
    assertThat(w, hasStyle("c"));
  }

  @Test
  public void whenTrueDoesNotFireInitialValueWhenFalse() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).set("c").on(w);
    assertThat(w, not(hasStyle("c")));
  }

  @Test
  public void whenTrueFiresWhenFalseChangesToTrue() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).set("c").on(w);
    b.set(true);
    assertThat(w, hasStyle("c"));
  }

  @Test
  public void whenTrueShowHidesWhenFalse() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).show(w);
    assertThat(w, is(hidden()));
  }

  @Test
  public void whenTrueShowDisplaysWhenTrue() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).show(w);
    assertThat(w, is(shown()));
  }

  @Test
  public void whenTrueShowHidesWhenChangesToFalse() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubWidget w = new StubWidget();
    binder.when(b).is(true).show(w);
    b.set(false);
    assertThat(w, is(hidden()));
  }

  @Test
  public void whenTrueAddDoesInitialSet() {
    final BooleanProperty b = booleanProperty("b", true);
    final ArrayList<String> list = new ArrayList<String>();
    binder.when(b).is(true).add("foo").to(list);
    assertThat(list, hasItem("foo"));
  }

  @Test
  public void whenTrueRemovesFromInitialSet() {
    final BooleanProperty b = booleanProperty("b", false);
    final ArrayList<String> list = new ArrayList<String>();
    list.add("foo");
    binder.when(b).is(true).add("foo").to(list);
    assertThat(list, not(hasItem("foo")));
  }

  @Test
  public void whenValueAddToListInitializesProperty() {
    final EnumProperty<Color> color = enumProperty("color");
    final ArrayList<String> list = new ArrayList<String>();
    list.add("foo");
    binder.when(color).is(Color.Blue).add("foo").to(list);
    assertThat(color.get(), is(Color.Blue));
    assertThat(color.isTouched(), is(false));
  }

  @Test
  public void whenValueAddToListDoesNotInitializeProperty() {
    final EnumProperty<Color> color = enumProperty("color");
    final ArrayList<String> list = new ArrayList<String>();
    // foo isn't in the list, so we leave color alone
    binder.when(color).is(Color.Blue).add("foo").to(list);
    assertThat(color.get(), is(nullValue()));
    assertThat(color.isTouched(), is(false));
  }

  @Test
  public void whenValueRemoveFromListInitializesProperty() {
    final EnumProperty<Color> color = enumProperty("color");
    final ArrayList<String> list = new ArrayList<String>();
    binder.when(color).is(Color.Blue).remove("foo").from(list);
    assertThat(color.get(), is(Color.Blue));
    assertThat(color.isTouched(), is(false));
  }

  @Test
  public void whenValueAddToListPropertyInitializesProperty() {
    final EnumProperty<Color> color = enumProperty("color");
    final ListProperty<String> list = listProperty("list");
    list.add("foo");
    binder.when(color).is(Color.Blue).add("foo").to(list);
    assertThat(color.get(), is(Color.Blue));
    assertThat(color.isTouched(), is(false));
  }

  @Test
  public void whenValueRemoveFromListPropertyInitializesProperty() {
    final EnumProperty<Color> color = enumProperty("color");
    final ListProperty<String> list = listProperty("list");
    binder.when(color).is(Color.Blue).remove("foo").from(list);
    assertThat(color.get(), is(Color.Blue));
    assertThat(color.isTouched(), is(false));
  }

  @Test
  public void enhanceIgnoresTabKeyUpEvent() {
    final StubTextBox b = new StubTextBox();
    final StringProperty s = stringProperty("s");
    binder.bind(s).to(b);
    binder.enhance(b);
    b.keyUp(KEY_TAB);
    assertThat(s.get(), is(nullValue()));
  }

  @Test
  public void whenTrueEnableLeavesEnabled() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubFocusWidget w = new StubFocusWidget();
    binder.when(b).is(true).enable(w);
    assertThat(w.isEnabled(), is(true));
  }

  @Test
  public void whenTrueEnableIsFalseThenSetsDisabled() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubFocusWidget w = new StubFocusWidget();
    binder.when(b).is(true).enable(w);
    assertThat(w.isEnabled(), is(false));
  }

  @Test
  public void whenTrueEnableChangesToFalseThenSetsDisabled() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubFocusWidget w = new StubFocusWidget();
    binder.when(b).is(true).enable(w);
    b.set(false);
    assertThat(w.isEnabled(), is(false));
  }

  @Test
  public void whenTrueDisabledChangesToDisabled() {
    final BooleanProperty b = booleanProperty("b", true);
    final StubFocusWidget w = new StubFocusWidget();
    binder.when(b).is(true).disable(w);
    assertThat(w.isEnabled(), is(false));
  }

  @Test
  public void whenTrueSetAnotherProperty() {
    final BooleanProperty b = booleanProperty("b", false);
    final IntegerProperty i = integerProperty("i", 1);
    binder.when(b).is(true).set(i).to(10);
    b.set(true);
    assertThat(i.get(), is(10));
  }

  @Test
  public void whenAlreadyTrueSetAnotherProperty() {
    final BooleanProperty b = booleanProperty("b", true);
    final IntegerProperty i = integerProperty("i", 1);
    binder.when(b).is(true).set(i).to(10);
    assertThat(i.get(), is(10));
  }

  @Test
  public void bindEnumCreatesItems() {
    final SetValue<Color> v = new SetValue<Color>("v", Color.Blue);
    final EnumProperty<Color> e = enumProperty(v);

    final StubListBox box = new StubListBox();
    binder.bind(e).to(box, Color.values());
    assertThat(box.getItemCount(), is(2));
    assertThat(box.getItemText(0), is("Blue"));
    assertThat(box.getItemText(1), is("Green"));
  }

  @Test
  public void bindEnumSetsInitialValue() {
    final SetValue<Color> v = new SetValue<Color>("v", Color.Blue);
    final EnumProperty<Color> e = enumProperty(v);

    final StubListBox box = new StubListBox();
    binder.bind(e).to(box, Color.values());
    assertThat(box.getSelectedIndex(), is(0));
  }

  @Test
  public void bindEnumAutoSelectsFirstValueIfNull() {
    final SetValue<Color> v = new SetValue<Color>("v", null);
    final EnumProperty<Color> e = enumProperty(v);

    final StubListBox box = new StubListBox();
    binder.bind(e).to(box, Color.values());
    assertThat(box.getSelectedIndex(), is(0));
    assertThat(v.get(), is(Color.Blue));
  }

  @Test
  public void bindEnumSetsInitialValueToOtherValue() {
    final SetValue<Color> v = new SetValue<Color>("v", Color.Green);
    final EnumProperty<Color> e = enumProperty(v);

    final StubListBox box = new StubListBox();
    binder.bind(e).to(box, Color.values());
    assertThat(box.getSelectedIndex(), is(1));
  }

  @Test
  public void bindEnumSetsValueOnChange() {
    final SetValue<Color> v = new SetValue<Color>("v", Color.Green);
    final EnumProperty<Color> e = enumProperty(v);

    final StubListBox box = new StubListBox();
    binder.bind(e).to(box, Color.values());
    box.select("Blue");
    assertThat(v.get(), is(Color.Blue));
  }

  @Test
  public void emptyStringIsTreatedAsNull() {
    s.set("a");
    binder.bind(s).to(box);
    box.setValue("", true);
    assertThat(s.get(), is(nullValue()));
  }

  @Test
  public void propertyToCookie() {
    final StubCookies cookies = new StubCookies();
    final StringCookie c = new StringCookie(cookies, "c");
    binder.bind(s).to(c);
    assertThat(s.get(), is(nullValue()));
    assertThat(cookies.get("c"), is(nullValue()));

    s.set("foo");
    assertThat(cookies.get("c"), is("foo"));
  }

  @Test
  public void propertyToCookieGetsInitialCookieValue() {
    final StubCookies cookies = new StubCookies();
    final StringCookie c = new StringCookie(cookies, "c");
    cookies.set("c", "foo");
    binder.bind(s).to(c);
    assertThat(s.get(), is("foo"));
    assertThat(s.isTouched(), is(false));
    assertThat(c.getValue(), is("foo"));
  }

  @Test
  public void propertyToCookieGetsInitialCookieValueUnlessAlreadyTouched() {
    final StubCookies cookies = new StubCookies();
    final StringCookie c = new StringCookie(cookies, "c");
    cookies.set("c", "foo");
    s.touch();
    binder.bind(s).to(c);
    assertThat(s.get(), is(nullValue()));
    assertThat(c.getValue(), is(nullValue()));
  }

  @Test
  public void propertyToCookieGetsInitialCookieValueUnlessAlreadySet() {
    final StubCookies cookies = new StubCookies();
    final StringCookie c = new StringCookie(cookies, "c");
    cookies.set("c", "foo");
    s.setValue("bar");
    s.setTouched(false); // make sure to untouch
    binder.bind(s).to(c);
    assertThat(s.get(), is("bar"));
    assertThat(c.getValue(), is("bar"));
  }

  @Test
  public void derivedValueToIsElement() {
    final StringProperty p = stringProperty(new DerivedValue<String>("p") {
      public String get() {
        return null;
      }
    });
    final StubElement e = new StubElement();
    // should skip the "set null property logic", otherwise will fail
    binder.bind(p).to(textOf(e));
  }

  @Test
  public void whenIsNull() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubWidget w = new StubWidget();
    binder.when(b).is(notNull()).set("c").on(w);
    assertThat(w, hasStyle("c"));
    b.set(null);
    assertThat(w, not(hasStyle("c")));
  }

  @Test
  public void whenSetToTakesValues() {
    final BooleanProperty b = booleanProperty("b", false);
    final StubElement e = new StubElement();
    binder.when(b).is(true).set(textOf(e)).to("t");
    b.set(true);
    assertThat(e.getInnerText(), is("t"));
  }

  @Test
  public void whenSetToOrElse() {
    final BooleanProperty b = booleanProperty("b");
    binder.when(b).is(true).set(s).toOrElse("a", "b");
    assertThat(s.get(), is("b"));
    b.set(true);
    assertThat(s.get(), is("a"));
    b.set(false);
    assertThat(s.get(), is("b"));
  }

  @Test
  public void propertyToHasText() {
    final StubLabel label = new StubLabel();
    final StringProperty b = stringProperty("b", "foo");
    binder.bind(b).to(textOf(label));
    // text is initially set
    assertThat(label.getText(), is("foo"));
    // and updated on change
    b.set("bar");
    assertThat(label.getText(), is("bar"));
  }

  @Test
  public void propertyToStringTrims() {
    binder.bind(s).to(box);
    box.type(" foo bar ");
    assertThat(s.get(), is("foo bar"));
    // to the property the value changed from null to "foo bar", so it updates the text box
    assertThat(box.getValue(), is("foo bar"));
  }

  @Test
  public void propertyToStringTrimsToNull() {
    binder.bind(s).to(box);
    box.type("  ");
    assertThat(s.get(), is(nullValue()));
    // to the property the value is still null, so it doesn't update the text box
    assertThat(box.getValue(), is("  "));
  }

  @Test
  public void commandPreventsEventDefault() {
    final DummyUiCommand command = new DummyUiCommand();
    final StubButton button = new StubButton();
    binder.bind(command).to(button);
    final StubClickEvent click = new StubClickEvent();
    button.fireEvent(click);
    assertThat(click.prevented, is(true));
  }

  @Test
  public void commandDisablesButton() {
    final DummyActiveCommand command = new DummyActiveCommand();
    final StubButton button = new StubButton();
    binder.bind(command).to(button);
    button.click();
    assertThat(button.isEnabled(), is(false));
    command.done();
    assertThat(button.isEnabled(), is(true));
  }

  @Test
  public void commandShowsSpinner() {
    final DummyActiveCommand command = new DummyActiveCommand();
    final StubButton button = new StubButton();
    final StubImage spinner = new StubImage();
    binder.bind(command).to(button).spin(spinner);
    assertThat(spinner, is(invisible()));
    button.click();
    assertThat(spinner, is(visible()));
    command.done();
    assertThat(spinner, is(invisible()));
  }

  @Test
  public void onClick() {
    final StubButton button = new StubButton();
    binder.onClick(button).set(s).to("clicked");
    button.click();
    assertThat(s.get(), is("clicked"));
  }

  @Test
  public void booleanToRadioGroupSetsTheInitialValueWhenNull() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b");
    binder.bind(b).to(b1, b2);
    assertThat(b1.getValue(), is(false));
    assertThat(b2.getValue(), is(true));
  }

  @Test
  public void booleanToRadioGroupSetsTheInitialValueWhenTrue() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", true);
    binder.bind(b).to(b1, b2);
    assertThat(b1.getValue(), is(true));
    assertThat(b2.getValue(), is(false));
  }

  @Test
  public void booleanToRadioGroupSetsTheInitialValueWhenFalse() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", false);
    binder.bind(b).to(b1, b2);
    assertThat(b1.getValue(), is(false));
    assertThat(b2.getValue(), is(true));
  }

  @Test
  public void booleanToRadioGroupSetsOnTrueClick() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", false);
    binder.bind(b).to(b1, b2);
    b1.click();
    assertThat(b.get(), is(true));
  }

  @Test
  public void booleanToRadioGroupSetsOnFalse() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", true);
    binder.bind(b).to(b1, b2);
    b2.click();
    assertThat(b.get(), is(false));
  }

  @Test
  public void booleanToRadioGroupSetsOnUpdateFalse() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", true);
    binder.bind(b).to(b1, b2);
    b.set(false);
    assertThat(b1.getValue(), is(false));
    assertThat(b2.getValue(), is(true));
  }

  @Test
  public void booleanToRadioGroupSetsOnUpdateTrue() {
    final StubRadioButton b1 = new StubRadioButton();
    final StubRadioButton b2 = new StubRadioButton();
    final BooleanProperty b = booleanProperty("b", false);
    binder.bind(b).to(b1, b2);
    b.set(true);
    assertThat(b1.getValue(), is(true));
    assertThat(b2.getValue(), is(false));
  }

  @Test
  public void stringToKeyUpSetsInitialValue() {
    s.set("a");
    binder.bind(s).toKeyUp(box);
    assertThat(box.getValue(), is("a"));
  }

  @Test
  public void stringToKeyUpUpdatesModelOnKeyUp() {
    binder.bind(s).toKeyUp(box);
    box.press('a');
    assertThat(s.get(), is("a"));
  }

  @Test
  public void stringToKeyUpDoesNotTrimOnPress() {
    binder.bind(s).toKeyUp(box);
    box.press('a');
    box.press(' ');
    assertThat(s.get(), is("a "));
  }

  @Test
  public void stringToKeyUpDoestTrimOnChange() {
    binder.bind(s).toKeyUp(box);
    box.type("a ");
    assertThat(s.get(), is("a"));
  }

  @Test
  public void stringToKeyUpUpdatesModelOnChange() {
    binder.bind(s).toKeyUp(box);
    box.type("a"); // just a change, no key up
    assertThat(s.get(), is("a"));
  }

  @Test
  public void stringToKeyUpUpdatesViewOnChange() {
    binder.bind(s).toKeyUp(box);
    s.set("a");
    assertThat(box.getValue(), is("a"));
  }

  @Test
  public void stringToKeyUpSetsMaxLength() {
    s.max(20);
    binder.bind(s).toKeyUp(box);
    assertThat(box.getMaxLength(), is(20));
  }

  @Test
  public void onClickToggleSetsNoInitialValue() {
    final BooleanProperty b = booleanProperty("b");
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).toggle(b);
    assertThat(b.get(), is(nullValue()));
  }

  @Test
  public void onClickToggleDoesActuallyToggle() {
    final BooleanProperty b = booleanProperty("b");
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).toggle(b);
    a.click();
    assertThat(b.get(), is(true));
    a.click();
    assertThat(b.get(), is(false));
  }

  @Test
  public void onClickTogglePreventsDefault() {
    final BooleanProperty b = booleanProperty("b");
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).toggle(b);
    final StubClickEvent c = new StubClickEvent();
    a.fireEvent(c);
    assertThat(c.prevented, is(true));
  }

  @Test
  public void onClickAdd() {
    ListProperty<String> strings = listProperty("strings");
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).add("a").to(strings);
    assertThat(strings.get().size(), is(0));
    a.click();
    assertThat(strings.get().size(), is(1));
  }

  @Test
  public void onClickRemove() {
    ListProperty<String> strings = listProperty("strings");
    strings.add("a");
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).remove("a").from(strings);
    assertThat(strings.get().size(), is(1));
    a.click();
    assertThat(strings.get().size(), is(0));
  }

  @Test
  public void onClickFocus() {
    final StubFocusWidget f = new StubFocusWidget();
    final StubAnchor a = new StubAnchor();
    binder.onClick(a).focus(f);
    a.click();
    assertThat(f.isFocused(), is(true));
  }

  @Test
  public void onClickPreventDefault() {
    binder.onClick(box).preventDefault();
    StubClickEvent e = new StubClickEvent();
    box.fireEvent(e);
    assertThat(e.prevented, is(true));
  }

  @Test
  public void onClickExecute() {
    DummyUiCommand c = new DummyUiCommand();
    binder.onClick(box).execute(c);
    box.click();
    assertThat(c.getExecutions(), is(1));
  }

  @Test
  public void onKeyDown() {
    final StubTextBox b = new StubTextBox();
    binder.onKeyDown(b).set(s).to("asdf");
    b.keyDown('a');
    assertThat(s.get(), is("asdf"));
  }

  @Test
  public void onKeyDownFiltered() {
    final StubTextBox b = new StubTextBox();
    binder.onKeyDown(b, KeyCodes.KEY_ENTER).set(s).to("asdf");
    b.keyDown('a');
    assertThat(s.get(), is(nullValue()));
    b.keyDown(KeyCodes.KEY_ENTER);
    assertThat(s.get(), is("asdf"));
  }

  @Test
  public void onChangeToggleSetsNoInitialValue() {
    final BooleanProperty b = booleanProperty("b");
    binder.onChange(box).toggle(b);
    assertThat(b.get(), is(nullValue()));
  }

  @Test
  public void onChangeToggleDoesActuallyToggle() {
    final BooleanProperty b = booleanProperty("b");
    binder.onChange(box).toggle(b);
    box.type("asdf");
    assertThat(b.get(), is(true));
    box.type("fdas");
    assertThat(b.get(), is(false));
  }

  @Test
  public void onPropertyChange() {
    DummyUiCommand c = new DummyUiCommand();
    binder.onChange(s).execute(c);
    s.set("asdf");
    assertThat(c.getExecutions(), is(1));
  }

  @Test
  public void onMemberChange() {
    DummyUiCommand c = new DummyUiCommand();
    DummyModel m = new DummyModel();
    binder.onMemberChange(m).execute(c);
    m.name.set("asdf");
    assertThat(c.getExecutions(), is(1));
  }

  @Test
  public void booleanToListAddsInitiallyWhenTrue() {
    BooleanProperty b = booleanProperty("b", true);
    List<String> names = new ArrayList<String>();
    binder.bind(b).to(names).has("foo");
    assertThat(names, hasItem("foo"));
  }

  @Test
  public void booleanToListInitiallyRemovesWhenTrue() {
    BooleanProperty b = booleanProperty("b", false);
    List<String> names = new ArrayList<String>();
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(names.size(), is(0));
  }

  @Test
  public void booleanToListAddsOnChange() {
    BooleanProperty b = booleanProperty("b", false);
    List<String> names = new ArrayList<String>();
    binder.bind(b).to(names).has("foo");
    assertThat(names.size(), is(0));
    b.set(true);
    assertThat(names, hasItem("foo"));
  }

  @Test
  public void booleanToListRemovesOnChange() {
    BooleanProperty b = booleanProperty("b", true);
    List<String> names = new ArrayList<String>();
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(names.size(), is(1));
    b.set(false);
    assertThat(names.size(), is(0));
  }

  @Test
  public void booleanToListInitializePropertyToTrue() {
    BooleanProperty b = booleanProperty("b");
    List<String> names = new ArrayList<String>();
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(b.get(), is(true));
    assertThat(b.isTouched(), is(false));
  }

  @Test
  public void booleanToListInitializePropertyToFalse() {
    BooleanProperty b = booleanProperty("b");
    List<String> names = new ArrayList<String>();
    binder.bind(b).to(names).has("foo");
    assertThat(b.get(), is(false));
    assertThat(b.isTouched(), is(false));
  }

  @Test
  public void booleanToListPropertyAddsInitiallyWhenTrue() {
    BooleanProperty b = booleanProperty("b", true);
    ListProperty<String> names = listProperty("names");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get(), hasItem("foo"));
  }

  @Test
  public void booleanToListPropertyInitiallyRemovesWhenTrue() {
    BooleanProperty b = booleanProperty("b", false);
    ListProperty<String> names = listProperty("names");
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get().size(), is(0));
  }

  @Test
  public void booleanToListPropertyAddsOnChange() {
    BooleanProperty b = booleanProperty("b", false);
    ListProperty<String> names = listProperty("names");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get().size(), is(0));
    b.set(true);
    assertThat(names.get(), hasItem("foo"));
  }

  @Test
  public void booleanToListPropertyRemovesOnChange() {
    BooleanProperty b = booleanProperty("b", true);
    ListProperty<String> names = listProperty("names");
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get().size(), is(1));
    b.set(false);
    assertThat(names.get().size(), is(0));
  }

  @Test
  public void booleanToListPropertyInitializePropertyToTrue() {
    BooleanProperty b = booleanProperty("b");
    ListProperty<String> names = listProperty("names");
    names.add("foo");
    binder.bind(b).to(names).has("foo");
    assertThat(b.get(), is(true));
    assertThat(b.isTouched(), is(false));
  }

  @Test
  public void booleanToListPropertyInitializePropertyToFalse() {
    BooleanProperty b = booleanProperty("b");
    ListProperty<String> names = listProperty("names");
    binder.bind(b).to(names).has("foo");
    assertThat(b.get(), is(false));
    assertThat(b.isTouched(), is(false));
  }

  @Test
  public void booleanToListPropertySetsTrueOnAdd() {
    BooleanProperty b = booleanProperty("b", false);
    ListProperty<String> names = listProperty("names");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get().size(), is(0));
    names.add("foo");
    assertThat(b.get(), is(true));
  }

  @Test
  public void booleanToListPropertySetsFalseOnRemove() {
    BooleanProperty b = booleanProperty("b", true);
    ListProperty<String> names = listProperty("names");
    binder.bind(b).to(names).has("foo");
    assertThat(names.get().size(), is(1));
    names.remove("foo");
    assertThat(b.get(), is(false));
  }

}
