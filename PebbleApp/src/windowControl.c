#include "windowControl.h"

Window* windowArray[NUM_WINDOWS];
TextLayer* alarmText;
int viewMode;

void stoplist_window_single_click_SELECT_handler(ClickRecognizerRef recognizer, void* context) {
  //Called when the MIDDLE button is clicked once.
  viewMode = (viewMode + 1) % NUM_VIEW_MODES;
  layer_mark_dirty(menu_layer_get_layer(menu_layer));

}
void stoplist_window_click_config_provider(Window *window) {
  //Function for setting callbacks for button clicks.
  window_single_click_subscribe(BUTTON_ID_SELECT, stoplist_window_single_click_SELECT_handler);
}

void show_get_off_alarm() {
  windowArray[WINDOW_ALARM] = window_create();
  window_stack_push(windowArray[WINDOW_ALARM], true /* Animated */);
  Layer *window_layer = window_get_root_layer(windowArray[WINDOW_ALARM]);
  GRect bounds = layer_get_frame(window_layer);
  alarmText = text_layer_create((GRect){ .origin = { 0, 30 }, .size = bounds.size });
  text_layer_set_text(alarmText, TEXT_GET_OFF);
}

void init_windows() {
  windowArray[WINDOW_3STOP] = window_create();
  window_stack_push(windowArray[WINDOW_3STOP], true /* Animated */);
  viewMode = VIEW_MODE_NAMES;
  // The click config for sending the test command to Android
  window_set_click_config_provider(windowArray[WINDOW_3STOP], (ClickConfigProvider)stoplist_window_click_config_provider);
  init_menu(windowArray[WINDOW_3STOP]);
}
