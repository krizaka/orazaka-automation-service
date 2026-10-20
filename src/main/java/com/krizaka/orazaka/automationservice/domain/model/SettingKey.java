package com.krizaka.orazaka.automationservice.domain.model;

/**
 * The business parameters of the automation service: keys of the active rule set ({@code
 * automation_settings.rules}), each with the default used when the key is absent and the hard
 * bounds code clamps it to (ADR-027, ADR-079 §1). Never a constant of the logic.
 */
public enum SettingKey {
  /** How many automations one owner may hold. */
  MAX_AUTOMATIONS_PER_OWNER("maxAutomationsPerUser", 20, 1, 200),
  /** The largest serialized size of an automation's fixed inputs. */
  MAX_INPUTS_BYTES("maxInputsBytes", 16_384, 256, 65_536);

  private final String key;
  private final int defaultValue;
  private final int minimum;
  private final int maximum;

  SettingKey(String key, int defaultValue, int minimum, int maximum) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.minimum = minimum;
    this.maximum = maximum;
  }

  /**
   * The key in the rule set document.
   *
   * @return the key in the rule set document
   */
  public String key() {
    return key;
  }

  /**
   * The value used when the row is absent or unreadable.
   *
   * @return the default
   */
  public int defaultValue() {
    return defaultValue;
  }

  /**
   * Keeps a stored value inside the bounds code accepts.
   *
   * @param value the stored value
   * @return the value, raised to the minimum or lowered to the maximum
   */
  public int clamp(int value) {
    return Math.max(minimum, Math.min(maximum, value));
  }
}
