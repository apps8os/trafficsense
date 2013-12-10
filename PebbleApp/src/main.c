#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"


#define MY_UUID { 0x83, 0xEE, 0xF3, 0x82, 0x21, 0xA4, 0x47, 0x3A, 0xA1, 0x89, 0xCE, 0xFF, 0xA4, 0x2F, 0x86, 0xB1 }
PBL_APP_INFO(MY_UUID,
             "Test App", "Software development project",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

Window window;
TextLayer hello_layer;

static void send_cmd(uint8_t cmd) {
	Tuplet value = TupletInteger(0, cmd);
	DictionaryIterator *iter;
	app_message_out_get(&iter);
	if (iter == NULL)
		return;
	dict_write_tuplet(iter, &value);
	dict_write_end(iter);
	app_message_out_send();
	app_message_out_release();
}

void up_click_handler(ClickRecognizerRef recognizer, Window *window) {
	send_cmd(0);
}

void click_config_provider(ClickConfig **config, void *context) {
    config[BUTTON_ID_UP]->click.handler = (ClickHandler)up_click_handler;
    config[BUTTON_ID_UP]->context = context;
    config[BUTTON_ID_UP]->click.repeat_interval_ms = 100;

}

void handle_init(AppContextRef ctx) {
  (void)ctx;

  window_init(&window, "Main window");
  window_stack_push(&window, true /* Animated */);
	text_layer_init(&hello_layer, GRect(0, 65, 144, 30));
	text_layer_set_text_alignment(&hello_layer, GTextAlignmentCenter);
	text_layer_set_text(&hello_layer, "Hello world!");
	text_layer_set_font(&hello_layer, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
	layer_add_child(&window.layer, &hello_layer.layer);
	window_set_click_config_provider(&window, (ClickConfigProvider)click_config_provider);
}

void my_out_sent_handler(DictionaryIterator *sent, void *context) {
  // outgoing message was delivered
	
}
void my_out_fail_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
  // outgoing message failed
}
void my_in_rcv_handler(DictionaryIterator *received, void *context) {
    Tuple* in_tuple = dict_find(received, 0);
	if (in_tuple) {
		text_layer_set_text(&hello_layer, in_tuple->value->cstring);
	}
}
void my_in_drp_handler(void *context, AppMessageResult reason) {
  // incoming message dropped
	text_layer_set_text(&hello_layer, "message dropped");
}

void pbl_main(void *params) {
  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
	.messaging_info = {
		.buffer_sizes = {
			.inbound = 64,
			.outbound = 16,
		},
		.default_callbacks.callbacks = {
      	  .out_sent = my_out_sent_handler,
     	  .out_failed = my_out_fail_handler,
     	  .in_received = my_in_rcv_handler,
          .in_dropped = my_in_drp_handler,
        },
	},
  };
  app_event_loop(params, &handlers);
}
