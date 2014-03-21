#include "main.h"

Stop stopArray[NUM_STOPS];
char currentLineCode[LINE_CODE_LENGTH];
char firstStopName[STOP_NAME_LENGTH];
char firstStopCode[STOP_CODE_LENGTH];

TimeOfDay startTime = {23, 59, 59}; // 23:59:59 for testing purpose

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
  uint32_t seconds = 0;
  seconds += (startTime.hours - currentTime.hours) * 3600;
  seconds += (startTime.minutes - currentTime.minutes) * 60;
  seconds += (startTime.seconds - currentTime.seconds);
  uint8_t hours = seconds / 3600;
  seconds -= hours * 3600;
  uint8_t minutes = seconds / 60;
  seconds -= minutes * 60;

  TimeOfDay timeToStart =   {hours, minutes, seconds};
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
  strncpy(firstStopName, "Some stop2", STOP_NAME_LENGTH-1);
  strncpy(firstStopCode, "1235", STOP_CODE_LENGTH-1);
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
