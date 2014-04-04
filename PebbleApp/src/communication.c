#include "communication.h"

void send_cmd(uint8_t cmd) {
	Tuplet value = TupletInteger(KEY_COMMAND, cmd);
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter); // returns AppMessageResult
	if (iter == NULL) // if app_message_outbox_begin fails, iter == NULL
		return;
	dict_write_tuplet(iter, &value);
	dict_write_end(iter);
	app_message_outbox_send(); //returns AppMessageResult
}

void message_received(DictionaryIterator *iterator) {
  //Automatically called when a message is received from the phone.
  Tuple *commandTuple = dict_find(iterator, KEY_COMMAND);
  if (commandTuple) {
    uint8_t command = commandTuple->value->data[0];
      //TODO: strings seem to be taken from the dictionary in a weird way:w
      if (command == COMMAND_GET_STOP) {
        // Get a single stop to a specified position of hte list
        uint8_t stopPosition = dict_find(iterator, KEY_STOP_NUM)->value->data[0]; // Position of the stop in the list
      	char* name = &dict_find(iterator, KEY_STOP_NAME)->value->cstring[0];
        char* code = &dict_find(iterator, KEY_STOP_CODE)->value->cstring[0];
        char* time = &dict_find(iterator, KEY_STOP_TIME)->value->cstring[0];
      	strncpy(stopArray[stopPosition].name, name, STOP_NAME_LENGTH);
        strncpy(stopArray[stopPosition].code, code, STOP_CODE_LENGTH);
        strncpy(stopArray[stopPosition].time, time, TIME_STR_LENGTH);
        if (stopPosition == NUM_STOPS -1) {
        	// Here we've received the last stop, so the segment iniiialization is complete
        	// Time to show the basic window
        	show_basic_window();
        }
      }
      else if (command == COMMAND_UPDATELIST) {
        // Update the list with a single stop
        // (move others up and push it to the boottom)
        for (int i = 1; i < NUM_STOPS - 1; i++) {
          stopArray[i-1] = stopArray[i];
        }
        int newPos = NUM_STOPS - 2;
        char* name = &dict_find(iterator, KEY_STOP_NAME)->value->cstring[0];
        char* code = &dict_find(iterator, KEY_STOP_CODE)->value->cstring[0];
        char* time = &dict_find(iterator, KEY_STOP_TIME)->value->cstring[0];
        strncpy(stopArray[newPos].name, name, STOP_NAME_LENGTH);
        strncpy(stopArray[newPos].code, code, STOP_CODE_LENGTH);
        strncpy(stopArray[newPos].time, time, TIME_STR_LENGTH);
      }
      else if (command == COMMAND_ALARM) {
        uint8_t alarmType = dict_find(iterator, KEY_ALARM)->value->data[0];
        if (alarmType == ALARM_GET_OFF) {
          alarm_get_off();
	}
      }
      else if (command == COMMAND_INIT_SEGMENT) {
        char* lineCode = &dict_find(iterator, KEY_LINE_NUMBER)->value->cstring[0];
	char* stopName = &dict_find(iterator, KEY_FIRST_STOP_NAME)->value->cstring[0];
	char* stopCode = &dict_find(iterator, KEY_FIRST_STOP_CODE)->value->cstring[0];
        uint8_t hour = dict_find(iterator, KEY_START_TIME_HOUR)->value->data[0];
        uint8_t min = dict_find(iterator, KEY_START_TIME_MIN)->value->data[0];
        uint8_t sec = dict_find(iterator, KEY_START_TIME_SEC)->value->data[0];
        startTime.hours = hour; startTime.minutes = min; startTime.seconds = sec;
        strncpy(currentLineCode, lineCode, LINE_CODE_LENGTH);
        strncpy(firstStopName, stopName, STOP_NAME_LENGTH);
        strncpy(firstStopCode, stopCode, STOP_CODE_LENGTH);
      }
      else if (command == COMMAND_SHOW_3STOP_WINDOW) {
	show_3stop_window();
      }
    
  }
  // Update the menu, otherwise the new stop will not be shown before it's selected
  // Marking dirty means telling the app that the layer has been updated and needs to be refreshed on the screen
  layer_mark_dirty(menu_layer_get_layer(menu_layer));
}

void message_dropped(DictionaryIterator *iterator) {
	// TODO: do something
}

void message_failed(DictionaryIterator *iterator, AppMessageResult reason, void *context) {
  
}

void message_sent(DictionaryIterator *iterator, void *context) {

}

void init_app_message() {
	// Start appmessage with an inbox (phone to watch) size of the first parameter and outbox of the second
	app_message_open(APP_MESSAGE_INBOX_SIZE_MINIMUM, 16);
	//set the function that will be called when a message is received from the phone
	app_message_register_inbox_received((AppMessageInboxReceived)message_received);
	//and when it's dropped
	app_message_register_inbox_dropped((AppMessageInboxDropped)message_dropped);
	
	//and for messages to phone (outbox)
	app_message_register_outbox_sent((AppMessageOutboxSent)message_sent);
	app_message_register_outbox_failed((AppMessageOutboxFailed)message_failed);
}
