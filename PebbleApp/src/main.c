#include "main.h"

Stop stopArray[NUM_STOPS];

Window *window;
MenuLayer *menu_layer;

uint16_t menu_get_num_sections_callback(MenuLayer *menu_layer, void *data) {
  return 2;
}

uint16_t menu_get_num_rows_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  switch (section_index) {
    case 0:
      return NUM_STOPS - 1;

    case 1:
      return 1;

    default:
      return 0;
  }
}

int16_t menu_get_header_height_callback(MenuLayer *menu_layer, uint16_t section_index, void *data) {
  switch (section_index) {
    // Section 0: Upcoming stops, return 0 not to show this header at all
    case 0:
      return 0;
    // Section 1: Last stop, return the default height to show the header
    case 1:
      return MENU_CELL_BASIC_HEADER_HEIGHT;
  }
  // Return 0 by default, if the section index is out of range for some reason
  return 0;
}

void menu_draw_header_callback(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
  // Determine which section we're working with
  switch (section_index) {
    case 0:
      // Draw title text in the section header
      menu_cell_basic_header_draw(ctx, cell_layer, "Next stops");
      break;

    case 1:
      menu_cell_basic_header_draw(ctx, cell_layer, "Last stop");
      break;
  }
}

void menu_draw_row_callback(GContext* ctx, const Layer *cell_layer, MenuIndex *cell_index, void *data) {
  // Determine which section we're going to draw in
  int row = cell_index->row;
  switch (cell_index->section) {
    case 0:
      // Use the row to specify which item we'll draw
	menu_cell_basic_draw(ctx, cell_layer, stopArray[row].name, stopArray[row].time, NULL);
    	break;

    case 1:
      menu_cell_basic_draw(ctx, cell_layer, stopArray[NUM_STOPS-1].name, stopArray[NUM_STOPS-1].time, NULL);
  }
}

// Here we capture when a user selects a menu item
void menu_select_callback(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
  // Do nothing
}

void message_received(DictionaryIterator *iterator) {
	//Automatically called when a message is received from the phone.
	for (int i = 0; i < NUM_STOPS; i++) {
		Tuple *stopTuple = dict_find(iterator, KEY_FIRST_STOP + i);
		if (stopTuple) {
			//text_layer_set_text(stopArray[i], stopTuple->value->cstring);
			char name[STOP_NAME_LENGTH];
			for (int i2 = 0; i2 < STOP_NAME_LENGTH; i2++) {
				name[i2] = stopTuple->value->cstring[i2];
			}
			strncpy(stopArray[i].name, name, STOP_NAME_LENGTH);
		}
	}
	// Update the menu, otherwise the new stop will not be shown before it's selected
	// Marking dirty means telling the app that the layer has been updated and needs to be refreshed on the screen
	layer_mark_dirty(menu_layer_get_layer(menu_layer));
		
}

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

void hellowindow_single_click_UP_handler(ClickRecognizerRef recognizer, void* context) {
	//Called when the UP button is clicked once.
	send_cmd(0);

}
void hellowindow_click_config_provider(Window *window) {
	//Function for setting callbacks for button clicks.
	window_single_click_subscribe(BUTTON_ID_UP, hellowindow_single_click_UP_handler);
}

void init_menu() {
	Layer *window_layer = window_get_root_layer(window);
  	GRect bounds = layer_get_frame(window_layer);
	menu_layer = menu_layer_create(bounds);

	menu_layer_set_callbacks(menu_layer, NULL, (MenuLayerCallbacks){
	    .get_num_sections = menu_get_num_sections_callback,
	    .get_num_rows = menu_get_num_rows_callback,
	    .get_header_height = menu_get_header_height_callback,
	    .draw_header = menu_draw_header_callback,
	    .draw_row = menu_draw_row_callback,
	    .select_click = menu_select_callback,
	});

	menu_layer_set_click_config_onto_window(menu_layer, window);
	layer_add_child(window_layer, menu_layer_get_layer(menu_layer));
}

void init(void) {
	//Initializes the app, called when the app is started, in main()
	window = window_create();
	window_stack_push(window, true /* Animated */);

	for (int i = 0; i < NUM_STOPS; i++) {
		strncpy(stopArray[i].name, "A stop", STOP_NAME_LENGTH-1);
		strncpy(stopArray[i].time, "00:00", TIME_STR_LENGTH-1);
	}
	
	init_menu();
	
	// The click config for sending the test command to Android
	//window_set_click_config_provider(window, (ClickConfigProvider)hellowindow_click_config_provider);
	
	// Start appmessage with an inbox (phone to watch) size of the first parameter and outbox of the second
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
	menu_layer_destroy(menu_layer);
	window_destroy(window);
}

int main(void) {
	init();
	app_event_loop();
	deinit();
}
