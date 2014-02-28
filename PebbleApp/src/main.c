#include "main.h"

Stop stopArray[NUM_STOPS];
char currentLineCode[LINE_CODE_LENGTH];
TimeOfDay startTime;

TimeOfDay getCurrentTime() {
  time_t now;
  struct tm *now_tm;
  int hour, min, sec;

  now = time(NULL);
  now_tm = localtime(&now);
  hour = now_tm->tm_hour;
  min = now_tm->tm_min;
  sec = now_tm->tm_sec;

  TimeOfDay currentTime = {hour, min, sec};
  return currentTime;
}

TimeOfDay getTimeToStart() {
  TimeOfDay currentTime = getCurrentTime();
  TimeOfDay timeToStart =   {startTime.hours - currentTime.hours,
                            startTime.minutes - currentTime.minutes,
                            startTime.seconds - currentTime.seconds};
  return timeToStart;
}

void init_stopArray() {
  for (int i = 0; i < NUM_STOPS; i++) {
    strncpy(stopArray[i].name, "A stop", STOP_NAME_LENGTH-1);
    strncpy(stopArray[i].code, "1234", STOP_CODE_LENGTH-1);
    strncpy(stopArray[i].time, "00:00", TIME_STR_LENGTH-1);
  }
}

void init(void) {
  //Initializes the app, called when the app is started, in main()
  strncpy(currentLineCode, "280", LINE_CODE_LENGTH-1);
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
