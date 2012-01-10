package org.tessell.model.dsl;

import org.tessell.model.events.PropertyChangedEvent;
import org.tessell.model.events.PropertyChangedHandler;
import org.tessell.model.properties.Property;
import org.tessell.model.validation.events.RuleTriggeredEvent;
import org.tessell.model.validation.events.RuleUntriggeredEvent;
import org.tessell.widgets.HasCss;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.HasEnabled;

public class WhenIsBinder<P> {

  private final Property<P> property;
  private final WhenCondition<P> condition;
  private boolean trigged = false;

  public WhenIsBinder(Property<P> property, WhenCondition<P> condition) {
    this.property = property;
    this.condition = condition;
  }

  public WhenIsSetBinder<P> set(String style) {
    return new WhenIsSetBinder<P>(property, condition, style);
  }

  public <V> WhenIsRemoveBinder<P, V> remove(V newValue) {
    return new WhenIsRemoveBinder<P, V>(property, condition, newValue);
  }

  public <V> WhenIsAddBinder<P, V> add(V newValue) {
    return new WhenIsAddBinder<P, V>(property, condition, newValue);
  }

  public HandlerRegistrations show(final HasCss... csses) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        showIfCondition(csses);
      }
    }));
    showIfCondition(csses); // set initial
    return hr;
  }

  public HandlerRegistrations hide(final HasCss... csses) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        hideIfCondition(csses);
      }
    }));
    hideIfCondition(csses); // set initial
    return hr;
  }

  public HandlerRegistrations visible(final HasCss css) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        visibleIfCondition(css);
      }
    }));
    visibleIfCondition(css); // set initial
    return hr;
  }

  public HandlerRegistrations error(final String message) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        errorIfCondition(message);
      }
    }));
    errorIfCondition(message);
    return hr;
  }

  public HandlerRegistrations enable(final HasEnabled enabled) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        updateEnabled(enabled, true);
      }
    }));
    updateEnabled(enabled, true); // set initial value
    return hr;
  }

  public HandlerRegistrations disable(final HasEnabled enabled) {
    HandlerRegistrations hr = new HandlerRegistrations();
    hr.add(property.addPropertyChangedHandler(new PropertyChangedHandler<P>() {
      public void onPropertyChanged(PropertyChangedEvent<P> event) {
        updateEnabled(enabled, false);
      }
    }));
    updateEnabled(enabled, false); // set initial value
    return hr;
  }

  private void updateEnabled(HasEnabled enabled, boolean valueIfTrue) {
    if (condition.evaluate(property)) {
      enabled.setEnabled(valueIfTrue);
    } else {
      enabled.setEnabled(!valueIfTrue);
    }
  }

  private void errorIfCondition(String message) {
    if (condition.evaluate(property)) {
      property.fireEvent(new RuleTriggeredEvent(this, message, new Boolean[] { false }));
      trigged = true;
    } else if (trigged) {
      property.fireEvent(new RuleUntriggeredEvent(this, message));
      trigged = false;
    }
  }

  private void showIfCondition(HasCss... csses) {
    if (condition.evaluate(property)) {
      for (HasCss css : csses) {
        css.getStyle().clearDisplay();
      }
    } else {
      for (HasCss css : csses) {
        css.getStyle().setDisplay(Display.NONE);
      }
    }
  }

  private void hideIfCondition(HasCss... csses) {
    if (condition.evaluate(property)) {
      for (HasCss css : csses) {
        css.getStyle().setDisplay(Display.NONE);
      }
    } else {
      for (HasCss css : csses) {
        css.getStyle().clearDisplay();
      }
    }
  }

  private void visibleIfCondition(HasCss css) {
    if (condition.evaluate(property)) {
      css.getStyle().clearVisibility();
    } else {
      css.getStyle().setVisibility(Visibility.HIDDEN);
    }
  }
}
