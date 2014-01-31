#include "windowControl.h"

Window* windowArray[NUM_WINDOWS];
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

void init_windows() {
  windowArray[WINDOW_3STOP] = window_create();
  window_stack_push(windowArray[WINDOW_3STOP], true /* Animated */);
  viewMode = VIEW_MODE_NAMES;
  // The click config for sending the test command to Android
  window_set_click_config_provider(windowArray[WINDOW_3STOP], (ClickConfigProvider)stoplist_window_click_config_provider);
  init_menu(windowArray[WINDOW_3STOP]);
}
