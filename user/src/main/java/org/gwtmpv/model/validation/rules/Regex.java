package org.gwtmpv.model.validation.rules;

import org.gwtmpv.model.properties.Property;
import org.gwtmpv.model.validation.Valid;

/** Validates that a property matches a regex. */
public class Regex extends AbstractRule<String, Regex> {

  // http://stackoverflow.com/questions/27745/getting-parts-of-a-url-regex#27755
  public static final String URL = "^((http[s]?):\\/)\\/([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";
  // Copy/paste from above without http(s)
  public static final String URL_NO_PROTOCOL = "^([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";
  // http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/df9ebce869e9c39d
  public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  // numeric
  public static final String NUMERIC = "^[0-9]+$";

  private final String regex;

  public Regex(final Property<String> property, final String message, final String regex) {
    super(property, message);
    this.regex = regex;
  }

  protected Valid isValid() {
    final String value = property.get();
    if (value == null) {
      return Valid.NO;
    }
    return value.matches(regex) ? Valid.YES : Valid.NO;
  }

  @Override
  protected Regex getThis() {
    return this;
  }

}