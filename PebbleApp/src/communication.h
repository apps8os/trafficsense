#ifndef __COMM_H__
#define __COMM_H__

#include "main.h"
#include "windowControl.h"

// Constants for receiving commands
#define KEY_COMMAND		0	// The key of the command tuple (the value of this tuple is the command)
// Constants related to the command for receiving a stop

// Command for receiving a stop to a defined place in the list
#define COMMAND_GET_STOP	0	
/* Command for receiving a stop, and updating the list to show the new stop
 and remove the old first stop. Uses the same keys as GET_STOP */
#define COMMAND_UPDATELIST	1

#define KEY_STOP_NUM		1	// The value of a tuple with this key defines where the stop is in the list	
#define KEY_STOP_NAME		2
#define KEY_STOP_CODE		3
#define KEY_STOP_TIME		4

// Receiving an alarm NOT USED RIGHT NOW!
#define COMMAND_ALARM		4
#define KEY_ALARM		1
#define ALARM_GET_OFF		0

// Segment initialization (waypoints are sent via COMMAND_GET_STOP one by one)
/* Command for getting values such as public transport line number that need to
be set when the segment has changed */
#define COMMAND_INIT_SEGMENT		2
// Key for the line number (e.g. 550, U etc.) of the next vehicle
#define KEY_LINE_NUMBER			1
#define KEY_FIRST_STOP_NAME		2 // Don't confuse these with KEY_STOP_NAME and -_CODE!
#define KEY_FIRST_STOP_CODE		3
#define KEY_START_TIME_HOUR		4
#define KEY_START_TIME_MIN		5
#define KEY_START_TIME_SEC		6

/*Command for switching out of the basic window, e.g. when the bus arrives early and we are already on the second
stop of the segment*/
#define COMMAND_SHOW_3STOP_WINDOW 3

// Initialize AppMessage
void init_app_message();
// Function to send a command with value cmd to Android. Currently not used.
void send_cmd(uint8_t cmd);
// This function is called when a message is received from the Android app
void message_received(DictionaryIterator *iterator);
// This function is called when a message from Android is dropped for some reason
void message_dropped(DictionaryIterator *iterator);

#endif
