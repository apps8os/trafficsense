#include "main.h"

Stop stopArray[NUM_STOPS];

void init_stopArray() {
  for (int i = 0; i < NUM_STOPS; i++) {
    strncpy(stopArray[i].name, "A stop", STOP_NAME_LENGTH-1);
    strncpy(stopArray[i].code, "1234", STOP_CODE_LENGTH-1);
    strncpy(stopArray[i].time, "00:00", TIME_STR_LENGTH-1);
  }
}

void init(void) {
  //Initializes the app, called when the app is started, in main()
  init_stopArray();
  init_windows();
  init_app_message();
}

void deinit(void) {
  menu_layer_destroy(menu_layer);
  for (int i = 0; i < NUM_WINDOWS; i++) {
    window_destroy(windowArray[i]);
  }
}

void alarm_get_off() {
  show_get_off_alarm();
}

int main(void) {
	init();
	app_event_loop();
	deinit();
}
