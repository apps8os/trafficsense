#include "communication.h"

void send_cmd(uint8_t cmd) {
	//Sends the value cmd to the phone as a tuple with key 0.
	Tuplet value = TupletInteger(0, cmd);
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
	    if (command == COMMAND_GET_STOP) {
	        uint8_t stopPosition = dict_find(iterator, KEY_STOP_NUM)->value->data[0]; // Position of the stop in the list
		char* name = &dict_find(iterator, KEY_STOP_NAME)->value->cstring[0];
	        char* code = &dict_find(iterator, KEY_STOP_CODE)->value->cstring[0];
	        char* time = &dict_find(iterator, KEY_STOP_TIME)->value->cstring[0];
		strncpy(stopArray[stopPosition].name, name, STOP_NAME_LENGTH);
	        strncpy(stopArray[stopPosition].code, code, STOP_CODE_LENGTH);
	        strncpy(stopArray[stopPosition].time, time, TIME_STR_LENGTH);
	    }
	}
	// Update the menu, otherwise the new stop will not be shown before it's selected
	// Marking dirty means telling the app that the layer has been updated and needs to be refreshed on the screen
	layer_mark_dirty(menu_layer_get_layer(menu_layer));
}

void message_dropped(DictionaryIterator *iterator) {
	// TODO: do something
}

void init_app_message() {
	// Start appmessage with an inbox (phone to watch) size of the first parameter and outbox of the second
	app_message_open(APP_MESSAGE_INBOX_SIZE_MINIMUM, 16);
	//set the function that will be called when a message is received from the phone
	app_message_register_inbox_received((AppMessageInboxReceived)message_received);
	//and when it's dropped
	app_message_register_inbox_dropped((AppMessageInboxDropped)message_dropped);
	
	//and for messages to phone (outbox)
	//app_message_register_outbox_sent(message_sent);
	//app_message_register_outbox_failed(message_failed);
}
