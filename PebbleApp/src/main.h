#ifndef __MAIN_H__
#define __MAIN_H__

#include <pebble.h>
#include "windowControl.h"
#include "communication.h"

#define NUM_STOPS		3	// Number of stops shown in the pebble screen, including the last stop
#define STOP_NAME_LENGTH	21
#define STOP_CODE_LENGTH	7
#define TIME_STR_LENGTH		6

#define TEXT_GET_OFF		"Get off!"

struct Stop {
	char name[STOP_NAME_LENGTH];
	char code[STOP_CODE_LENGTH];
	char time[TIME_STR_LENGTH];
}; typedef struct Stop Stop;

extern Stop stopArray[NUM_STOPS];

// Initializes the app, called when the app is started, in main()
void init(void);
void deinit(void);
int main(void);
void alarm_get_off();

#endif
