#ifndef __COMM_H__
#define __COMM_H__

#include "main.h"

// Constants for receiving commands
#define KEY_COMMAND		0	// The key of the command tuple (the value of this tuple is the command)
// Constants related to the command for receiving a stop
#define COMMAND_GET_STOP	0

#define KEY_STOP_NUM		1	// The value of a tuple with this key defines where the stop is in the list	
#define KEY_STOP_NAME		2
#define KEY_STOP_CODE		3
#define KEY_STOP_TIME		4

#define COMMAND_ALARM		1
#define KEY_ALARM		1
#define ALARM_GET_OFF		0

// Initialize AppMessage
void init_app_message();
// Function to send a command with value cmd to Android. Currently not used.
void send_cmd(uint8_t cmd);
// This function is called when a message is received from the Android app
void message_received(DictionaryIterator *iterator);
// This function is called when a message from Android is dropped for some reason
void message_dropped(DictionaryIterator *iterator);

#endif