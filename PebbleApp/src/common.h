#ifndef __COMMON_H__
#define __COMMON_H__

#include <pebble.h>

/**
 * Constants for receiving commands
 */
/**
 * Constants related to the command for receiving a stop
 */
// Command for receiving a stop to a defined place in the list.
#define COMMAND_GET_STOP	0
/**
 *  Command for receiving a stop, and updating the list to show the
 *  new stop and remove the old first stop. Uses the same keys as GET_STOP
 */
#define COMMAND_UPDATELIST	1
/**
 * The value of a tuple with this key defines where the stop is in the list.
 */
// The key of the command tuple (the value of this tuple is the command)
#define KEY_COMMAND		0
#define KEY_STOP_NUM		1
#define KEY_STOP_NAME		2
#define KEY_STOP_CODE		3
#define KEY_STOP_TIME		4
/**
 * Receiving an alarm
 */
#define COMMAND_ALARM		4
#define KEY_ALARM		1
#define ALARM_GET_OFF		0
/**
 * Segment initialization and waypoints are sent via
 * COMMAND_GET_STOP one by one. Command for getting values
 * such as public transport line number that need to be set when the
 * segment has changed.
 */
#define COMMAND_INIT_SEGMENT		2
/**
 * Key for the line number (e.g. 550, U etc.) of the next vehicle.
 * Do not confuse these with KEY_STOP_NAME and -_CODE!
 */
#define KEY_LINE_NUMBER			1
#define KEY_FIRST_STOP_NAME		2
#define KEY_FIRST_STOP_CODE		3
#define KEY_START_TIME_HOUR		4
#define KEY_START_TIME_MIN		5
#define KEY_START_TIME_SEC		6
/**
 * Command for switching out of the basic window,
 * e.g. when the bus arrives early and we are already
 * on the second stop of the segment.
 */
#define COMMAND_SHOW_3STOP_WINDOW 3
/**
 *  Commands that can be sent from pebble to Android side.
 */
// Request for data from Android side.
#define PEBBLE_COMMAND_GET 	0

/**
 * Number of stops shown on Pebble screen, including the last stop.
 */
#define NUM_STOPS			3

#define STOP_NAME_LENGTH	21
#define STOP_CODE_LENGTH	7
#define TIME_STR_LENGTH		6
// Max length of a public transport line code.
#define LINE_CODE_LENGTH	5

#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

#define ERROR_WAITING		0 // Not really an error.. just tell the user to wait.
#define ERROR_NO_CONNECTION	1
#define ERROR_NOT_READY		2

#define NUM_WINDOWS		3
#define WINDOW_BASIC		0
#define WINDOW_3STOP		1
#define WINDOW_ERROR	2

#define NUM_VIEW_MODES		2
#define VIEW_MODE_NAMES		0
#define VIEW_MODE_CODES		1

#define TEXT_GET_OFF		"Get off!"

/**
 * Usage example: strcpy(dest, timeUnitsStr[UNIT_MINUTES])
 */
#define UNIT_HOURS		2
#define UNIT_MINUTES		1
#define UNIT_SECONDS		0
#define UNIT_HOURS_STR     "hours"
#define UNIT_MINUTES_STR   "minutes"
#define UNIT_SECONDS_STR   "seconds"


struct Stop {
	char name[STOP_NAME_LENGTH];
	char code[STOP_CODE_LENGTH];
	char time[TIME_STR_LENGTH];
}; typedef struct Stop Stop;

struct TimeOfDay {
	uint8_t hours;
	uint8_t minutes;
	uint8_t seconds;
}; typedef struct TimeOfDay TimeOfDay;


extern MenuLayer *menu_layer;

extern Stop stopArray[NUM_STOPS];
/**
 * Code of the current public transport line of the current segment
 */
extern char currentLineCode[LINE_CODE_LENGTH];
extern char firstStopName[STOP_NAME_LENGTH];
extern char firstStopCode[STOP_CODE_LENGTH];
/**
 * Time when vehicle leaves the first stop.
 */
extern TimeOfDay startTime;

extern int viewMode;
extern Window* windowArray[NUM_WINDOWS];

// Get time to start of segment.
TimeOfDay getTimeToStart();
// Present a Get Off alarm.
void alarm_get_off();
//
void init_windows();
// Pop up the get off alarm window and vibrate
void show_get_off_alarm();
// TODO
void show_3stop_window();
// TODO
void show_basic_window();
// Initialize the menu for the given window
void init_menu(Window* window);
// Initialize AppMessage
void init_app_message();

/**
 * Function to send a command with value cmd to Android.
 * Currently unused.
 */
void send_cmd(uint8_t cmd);


#endif /* __COMMON_H__ */

