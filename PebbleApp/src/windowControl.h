#ifndef __WIN_CNTL_H__
#define __WIN_CNTL_H__

#include "menu.h"

#define NUM_WINDOWS		2
#define WINDOW_BASIC		0
#define WINDOW_3STOP		1

#define NUM_VIEW_MODES		2
#define VIEW_MODE_NAMES		0
#define VIEW_MODE_CODES		1

extern int viewMode;
extern Window* windowArray[NUM_WINDOWS];

void init_windows();
// Called when the MIDDLE button is clicked once.
void stoplist_window_single_click_SELECT_handler(ClickRecognizerRef recognizer, void* context);
// Function for setting callbacks for button clicks.
void stoplist_window_click_config_provider(Window *window);

#endif
