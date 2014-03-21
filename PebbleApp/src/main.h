#ifndef __MAIN_H__
#define __MAIN_H__

#include <pebble.h>
#include "windowControl.h"
#include "communication.h"

#define NUM_STOPS			3	// Number of stops shown in the pebble screen, including the last stop
#define STOP_NAME_LENGTH	21
#define STOP_CODE_LENGTH	7
#define TIME_STR_LENGTH		6

#define LINE_CODE_LENGTH	5	// Max length of a public transport line code

#define TEXT_GET_OFF		"Get off!"

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

extern Stop stopArray[NUM_STOPS];
// Code of the current public transport line of the current segment
extern char currentLineCode[LINE_CODE_LENGTH];
extern char firstStopName[STOP_NAME_LENGTH];
extern char firstStopCode[STOP_CODE_LENGTH];
extern TimeOfDay startTime; // Time when vehicle leaves the first stop

// Get time to start of segment
TimeOfDay getTimeToStart();
// Get current time of day
TimeOfDay getCurrentTime();
// Initializes the app, called when the app is started, in main()
void init(void);
void deinit(void);
int main(void);
void alarm_get_off();

#endif
