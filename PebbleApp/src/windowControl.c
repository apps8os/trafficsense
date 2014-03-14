#include "windowControl.h"

Window* windowArray[NUM_WINDOWS]; // Array of all windows
int currentWindow; // Currently active window
Window* alarmWindow;
TextLayer* alarmText;
int viewMode;

// Bassic window related variables
TextLayer* lineCode;
TextLayer* timeUnit;
TextLayer* timeAmount;
TextLayer* stopCodeAndName;
static char timeAmountStr[4];
char stopCodeAndNameBuff[30];

void stoplist_window_single_click_SELECT_handler(ClickRecognizerRef recognizer, void* context) {
  //Called when the MIDDLE button is clicked once.
  viewMode = (viewMode + 1) % NUM_VIEW_MODES;
  layer_mark_dirty(menu_layer_get_layer(menu_layer));
}

void window_single_click_UP_handler(ClickRecognizerRef recognizer, void* context) {
  currentWindow = (currentWindow + 1) % NUM_VIEW_MODES;
  window_stack_pop(true);
  window_stack_push(windowArray[currentWindow], true);
}

void window_single_click_DOWN_handler(ClickRecognizerRef recognizer, void* context) {
  currentWindow = ((currentWindow - 1) % NUM_VIEW_MODES) * -1;
  window_stack_pop(true);
  window_stack_push(windowArray[currentWindow], true);
}

void click_config_provider(Window *window) {
  //Function for setting callbacks for button clicks.
  if (window == windowArray[WINDOW_3STOP]) {
    window_single_click_subscribe(BUTTON_ID_SELECT, stoplist_window_single_click_SELECT_handler);
  }
  // In the current version of the UI the user is not allowed to change the window
  //window_single_click_subscribe(BUTTON_ID_UP, window_single_click_UP_handler);
  //window_single_click_subscribe(BUTTON_ID_DOWN, window_single_click_DOWN_handler);
}

void show_get_off_alarm() {
  //Right now this function is not in use
  alarmWindow = window_create();
  window_stack_push(alarmWindow, true /* Animated */);
  Layer *window_layer = window_get_root_layer(alarmWindow);
  GRect bounds = layer_get_frame(window_layer);
  alarmText = text_layer_create((GRect){ .origin = { 0, 30 }, .size = bounds.size });
  text_layer_set_text(alarmText, TEXT_GET_OFF);
}

void set_time_text_by_unit(int unit) {
  // Set the amount of time to the current value
  switch (unit) {
    // TODO: stop using the magic variable 4 for the length
    case UNIT_HOURS:
      snprintf(timeAmountStr, 4, "%d", getTimeToStart().hours);
      break;
    case UNIT_MINUTES:
      snprintf(timeAmountStr, 4, "%d", getTimeToStart().minutes);
      break;
    case UNIT_SECONDS:
      snprintf(timeAmountStr, 4, "%d", getTimeToStart().seconds);
      break;
  }
  text_layer_set_text(timeAmount, timeAmountStr);
  text_layer_set_text(timeUnit, timeUnitsStr[unit]);
}

void basic_window_loop() {
  uint32_t timeout_ms;
  TimeOfDay timeToStart = getTimeToStart();
  if (timeToStart.hours < 1) {
    if (timeToStart.minutes < 1) {
      if (timeToStart.seconds < 1) {
        show_3stop_window();
        return; // stop the loop when time has run out 
      } else {
        timeout_ms = 1000;
        set_time_text_by_unit(UNIT_SECONDS);
      }
    } else {
      timeout_ms = 60000;
      set_time_text_by_unit(UNIT_MINUTES);
    }
  } else {
    timeout_ms = 60000 * 60;
    set_time_text_by_unit(UNIT_HOURS);
  }
  app_timer_register(timeout_ms, (AppTimerCallback)basic_window_loop, NULL);
}

void show_basic_window() {
  // TODO: Make sure the buffer size is enough
  stopCodeAndNameBuff[0] = '\0';
  strncat(stopCodeAndNameBuff, stopArray[0].code, 30);
  strncat(stopCodeAndNameBuff, " ", 30);
  strncat(stopCodeAndNameBuff, stopArray[0].name, 30);
  text_layer_set_text(stopCodeAndName, stopCodeAndNameBuff); // Set the text of the stop
  text_layer_set_text(lineCode, currentLineCode);
  currentWindow = WINDOW_BASIC;
  window_stack_pop(true); // Remove 3stop-window from the'window stack
  window_stack_push(windowArray[WINDOW_BASIC], true); // Push the basic window (show it)
  basic_window_loop();
}

void show_3stop_window() {
  if (currentWindow != WINDOW_3STOP) {
    currentWindow = WINDOW_3STOP;
    window_stack_pop(true); // Remove basic window from the'window stack
    window_stack_push(windowArray[WINDOW_3STOP], true); // Push the 3stop window (show it)
  }
}

void init_basic_window() {
  // Initialize the time units

  windowArray[WINDOW_BASIC] = window_create();
  window_stack_push(windowArray[WINDOW_BASIC], true);
  window_set_click_config_provider(windowArray[WINDOW_BASIC], (ClickConfigProvider)click_config_provider);
  Layer *window_layer = window_get_root_layer(windowArray[WINDOW_BASIC]);
  GRect bounds = layer_get_frame(window_layer);

  // Initialize the line code text layer
  lineCode = text_layer_create((GRect){ .origin = { 0, 30 }, .size = bounds.size });
  text_layer_set_text(lineCode, currentLineCode);
  layer_add_child(window_layer, text_layer_get_layer(lineCode));

  // Initialize the text layer which shows the amount of time left
  timeAmount = text_layer_create((GRect){ .origin = { 0, 60 }, .size = bounds.size });
  layer_add_child(window_layer, text_layer_get_layer(timeAmount));
  
  // Initialize the text layer which shows the units for the shown time
  timeUnit = text_layer_create((GRect){ .origin = { 30, 60 }, .size = bounds.size });
  text_layer_set_text(timeUnit, timeUnitsStr[0]);
  layer_add_child(window_layer, text_layer_get_layer(timeUnit));

  // Initialize the text layer which shows the first stop name and code
  stopCodeAndName = text_layer_create((GRect){ .origin = { 0, 90 }, .size = bounds.size });
  text_layer_set_text(stopCodeAndName, "E9999 Some stop");
  layer_add_child(window_layer, text_layer_get_layer(stopCodeAndName));

  // Start the window loop, which updates the amount of time each second/minute
  basic_window_loop();
  
}

void init_windows() {
  currentWindow = WINDOW_BASIC;
  init_basic_window();

  windowArray[WINDOW_3STOP] = window_create();
  //window_stack_push(windowArray[WINDOW_3STOP], true /* Animated */);
  viewMode = VIEW_MODE_NAMES;
  // The click config for sending the test command to Android
  window_set_click_config_provider(windowArray[WINDOW_3STOP], (ClickConfigProvider)click_config_provider);
  init_menu(windowArray[WINDOW_3STOP]);
}
