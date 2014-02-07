#include "windowControl.h"

Window* windowArray[NUM_WINDOWS];
int currentWindow;
Window* alarmWindow;
TextLayer* alarmText;
int viewMode;

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
  window_single_click_subscribe(BUTTON_ID_UP, window_single_click_UP_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, window_single_click_DOWN_handler);
}

void show_get_off_alarm() {
  alarmWindow = window_create();
  window_stack_push(alarmWindow, true /* Animated */);
  Layer *window_layer = window_get_root_layer(alarmWindow);
  GRect bounds = layer_get_frame(window_layer);
  alarmText = text_layer_create((GRect){ .origin = { 0, 30 }, .size = bounds.size });
  text_layer_set_text(alarmText, TEXT_GET_OFF);
}

void init_windows() {
  currentWindow = 0;
  windowArray[WINDOW_BASIC] = window_create();
  window_stack_push(windowArray[WINDOW_BASIC], true);
  window_set_click_config_provider(windowArray[WINDOW_BASIC], (ClickConfigProvider)click_config_provider);

  windowArray[WINDOW_3STOP] = window_create();
  //window_stack_push(windowArray[WINDOW_3STOP], true /* Animated */);
  viewMode = VIEW_MODE_NAMES;
  // The click config for sending the test command to Android
  window_set_click_config_provider(windowArray[WINDOW_3STOP], (ClickConfigProvider)click_config_provider);
  init_menu(windowArray[WINDOW_3STOP]);
}
