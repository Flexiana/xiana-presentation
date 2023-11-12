# Xiana-presentation

For Xiana webinar Jun 20 2023.

## See the webinar

https://www.youtube.com/watch?v=VuO0gcOHvKU

## Source code of the presentation

https://github.com/Flexiana/xiana-webinar_source-code

## Overview

Presentation via modifying view-box attribute on an SVG file.

## Development

### To get an interactive development environment run:

    lein fig:build

### Use your mouse

to scroll and zoom in the browser window

### To get the actual values

of view-box press `s`, and copy the values from the js/prompt.

### Slides stored in

`xiana-prezi.core/states` it should be edited by hand.

### Slides can be recalled by order

using cursor left and right buttons

To clean all compiled files:

	lein clean

To create a production build run:

	lein clean
	lein fig:min

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
