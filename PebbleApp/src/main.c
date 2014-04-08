#include "common.h"

Stop stopArray[NUM_STOPS];
/**
 * Code of the current public transport line of the current segment
 */
char currentLineCode[LINE_CODE_LENGTH];
char firstStopName[STOP_NAME_LENGTH];
char firstStopCode[STOP_CODE_LENGTH];

TimeOfDay startTime = {23, 59, 59}; // 23:59:59 for testing purpose

/**
 * Get current time of day.
 */
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

/**
 * Get time to start of segment.
 */
TimeOfDay getTimeToStart() {
  TimeOfDay currentTime = getCurrentTime();
  int startTimeSec = 0;
  startTimeSec += startTime.hours * 3600;
  startTimeSec += startTime.minutes * 60;
  startTimeSec += startTime.seconds;

  int currentTimeSec = 0;
  currentTimeSec += currentTime.hours * 3600;
  currentTimeSec += currentTime.minutes * 60;
  currentTimeSec += currentTime.seconds;

  int seconds = startTimeSec - currentTimeSec;
  if (seconds < 0) {
    // Goes over midnight
    int midnightSec = 24 * 3600;
    seconds = midnightSec - currentTimeSec + startTimeSec;
  }
  uint8_t hours = seconds / 3600;
  seconds -= hours * 3600;
  uint8_t minutes = seconds / 60;
  seconds -= minutes * 60;

  TimeOfDay timeToStart =   {hours, minutes, seconds};
  return timeToStart;
}

/**
 * TODO: documentation.
 */
void init_stopArray() {
  for (int i = 0; i < NUM_STOPS; i++) {
    strncpy(stopArray[i].name, "A stop", STOP_NAME_LENGTH-1);
    strncpy(stopArray[i].code, "1234", STOP_CODE_LENGTH-1);
    strncpy(stopArray[i].time, "00:00", TIME_STR_LENGTH-1);
  }
}

/**
 * Initializes the application.
 * Called in main().
 */
void init(void) {
  // TODO: Some initial place holders ...
  strncpy(currentLineCode, "280", LINE_CODE_LENGTH-1);
  strncpy(firstStopName, "Some stop2", STOP_NAME_LENGTH-1);
  strncpy(firstStopCode, "1235", STOP_CODE_LENGTH-1);
  init_stopArray();
  init_windows();
  init_app_message();
  // Gets required information from Android side.
  send_cmd(PEBBLE_COMMAND_GET);
}

/**
 * Clean up.
 */
void deinit(void) {
  menu_layer_destroy(menu_layer);
  for (int i = 0; i < NUM_WINDOWS; i++) {
    window_destroy(windowArray[i]);
  }
}

/**
 * Present a Get Off alarm.
 * Called by communication.c:message_received()
 */
void alarm_get_off() {
  show_get_off_alarm();
}

/**
 * Pebble application starts here.
 */
int main(void) {
  init();
  app_event_loop();
  deinit();
}
