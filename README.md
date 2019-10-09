# Demo

This script sends out a random chirp, then continuously listens out for chirps.
It requires a [default] block to be set up in your `~/.chirprc` file. See
[developers.chirp.io](https://developers.chirp.io) for further information.

If you want to select another block in the `~/.chirprc` other than the [default],
then pass in the block name with the `-c` argument. For example to select the
[ultrasonic] block in your `~/.chirprc`.

    python3 example.py -c ultrasonic

The script will print out the available audio i/o devices, and point to the
default audio devices. You may find that on some platforms you may find you need to
explicitly set the input/output device. Use the desired device index with the
`-i` and `-o` parameters accordingly to change devices.

There are also options to alter the block size and sample rate, however you
shouldn't need to use these.

## Usage

```bash
ChirpSDK Demo

optional arguments:
  -h, --help  show this help message and exit
  -c C        The configuration block [name] in your ~/.chirprc file
              (optional)
  -i I        Input device index (optional)
  -o O        Output device index (optional)
  -b B        Block size (optional)
  -s S        Sample rate (optional)

Sends a random chirp payload, then continuously listens for chirps
```

