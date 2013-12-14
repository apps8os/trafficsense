#include <pebble.h>

static Window * window;
static TextLayer *text_layer;

void message_received(DictionaryIterator *iterator) {
	Tuple *tupleWithKey0 = dict_find(iterator, 0);
	if (tupleWithKey0) {
		text_layer_set_text(text_layer, tupleWithKey0->value->cstring);
	}
		
}

static void send_cmd(uint8_t cmd) {
	Tuplet value = TupletInteger(0, cmd);
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter); // returns AppMessageResult
	if (iter == NULL) // if app_message_outbox_begin fails, iter == NULL
		return;
	dict_write_tuplet(iter, &value);
	dict_write_end(iter);
	app_message_outbox_send(); //returns AppMessageResult
}

void hellowindow_single_click_handler(ClickRecognizerRef recognizer, void* context) {
	text_layer_set_text(text_layer, "Up button pressed");
	send_cmd(0);

}
void hellowindow_click_config_provider(Window *window) {
	window_single_click_subscribe(BUTTON_ID_UP, hellowindow_single_click_handler);
}

void init(void) {
	window = window_create();
	window_stack_push(window, true /* Animated */);
	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_frame(window_layer);
	text_layer = text_layer_create((GRect){ .origin = { 0, 30 }, .size = bounds.size });
	text_layer_set_text(text_layer, "Press up to send to phone");
	text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
	layer_add_child(window_layer, text_layer_get_layer(text_layer));
	
	
	window_set_click_config_provider(window, (ClickConfigProvider)hellowindow_click_config_provider);
	
	
	app_message_open(64, 16);
	//set the function that will be called when a message is received from the phone
	app_message_register_inbox_received((AppMessageInboxReceived)message_received);
	//and when it's dropped
	//app_message_register_inbox_dropped(message_dropped)
		
	//and for messages to phone (outbox)
	//app_message_register_outbox_sent(message_sent);
	//app_message_register_outbox_failed(message_failed);
	
}

void deinit(void) {
	text_layer_destroy(text_layer);
	window_destroy(window);
}

int main(void) {
	init();
	app_event_loop();
	deinit();
}
