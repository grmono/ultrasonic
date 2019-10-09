import argparse
import sys
import time

from chirpsdk import ChirpSDK, CallbackSet, CHIRP_SDK_STATE

global args
global MSG_POOL
global RUN
RUN = 0
MSG_POOL = []

WAIT_TIME = 5

class dotdict(dict):
    """dot.notation access to dictionary attributes"""
    __getattr__ = dict.get
    __setattr__ = dict.__setitem__
    __delattr__ = dict.__delitem__


class Callbacks(CallbackSet):

    def on_state_changed(self, previous_state, current_state):
        """ Called when the SDK's state has changed """
        print('State changed from {} to {}'.format(
            CHIRP_SDK_STATE.get(previous_state),
            CHIRP_SDK_STATE.get(current_state)))

    def on_sending(self, payload, channel):
        """ Called when a chirp has started to be transmitted """
        print('Sending: {data} [ch{ch}]'.format(
            data=list(payload), ch=channel))

    def on_sent(self, payload, channel):
        """ Called when the entire chirp has been sent """
        print('Sent: {data} [ch{ch}]'.format(
            data=list(payload), ch=channel))

    def on_receiving(self, channel):
        """ Called when a chirp frontdoor is detected """
        print('Receiving data [ch{ch}]'.format(ch=channel))

    def on_received(self, payload, channel):
        """
        Called when an entire chirp has been received.
        Note: A payload of None indicates a failed decode.
        """
        if payload is None:
            print('Decode failed!')
            return
        else:
            print('Received: {data} [ch{ch}]'.format(
                data=list(payload), ch=channel))

        # Call error correction verifier
        operation = self.verify(payload)
        # Verify if the prinout is needed or another operation has been performed
        if not operation:
            stream = ""
            for number in payload:
                value = str(hex(number))
                stream = stream + value
            print("Received hex value: {}".format(stream))

            string_received = ""
            for integer in payload:
                character = str(chr(integer))
                string_received = string_received + character
            print("Receveid message: {}",format(string_received))

    def initialize(self):
        "Validates if the protocol option has benn chosen"
        global args
        self.args = args
        if not args.mode:
            return False
        else:
            return True

    def verify(self, payload):
        """Verify the payload"""
        if not self.initialize():
            return False
        # Verify length of payload
        if args.mode == "full":
            return self.pool_spot_assigner(payload)
        else:
            return self.simple_protocol(payload)

    def get_msg_from_pool(self, id):
        """Rerieves an object from the pool"""
        global MSG_POOL

        for object in MSG_POOL:
            object = dotdict(object)
            if id == object.id:
                return object.msg
        return False

    def get_index_of_id(self, id):
        global MSG_POOL
        i = 0
        for object in MSG_POOL:
            object = dotdict(object)
            if id == object.id:
                return i
            i = i + 1
        return False

    def verify_payload_length(self, payload):
        if len(payload) == 4:
            return True
        elif len(payload) == 8:
            return True

        return False

    def pool_spot_assigner(self, payload):
        """Decides if a message should be processed or placed in queue"""
        global MSG_POOL

        print("Initiating pool assigning")
        if not self.verify_payload_length(payload):
            print("Failed to verify PDU length for error mode")
            return False

        elif len(payload) == 4 and payload[0] == 102:
            print("Detected arming payload")
            if not self.get_msg_from_pool(payload[1]):
                MSG_POOL.append({'id': payload[1], 'msg':""})
                print("Armed cache for msg id: {}".format(payload[1]))
                return True

        elif len(payload) == 8:
            print("Detected msg continiouation")
            msg = self.get_msg_from_pool(payload[0])
            if msg or msg == "":
                index = self.get_index_of_id(payload[0])
                payload.remove(102)
                MSG_POOL[index]['msg'] = MSG_POOL[index]['msg'] + payload
        else:
            print("Received invalid payload, dropping...")

        return False

    def simple_protocol(self, payload):
        global RUN
        global MSG_POOL

        print("Processing payload: {}".format(payload))
        string_received = ""
        for integer in payload:
            character = str(chr(integer))
            string_received = string_received + character

        if RUN == 0:
            MSG_POOL.append(string_received)
        else:
            MSG_POOL[0] = MSG_POOL[0] + string_received

        if string_received == "ffffffff":
            print("Cleaning up message")
            msg = MSG_POOL[0]

            print("Received MSG: {}".format(msg[:-8]))
            MSG_POOL[0] = ""
        RUN = 1
        return True

def asci_to_integer_list(asci):
    message = []
    stream = list(asci)
    # Generate integer values representing the ASCI character
    for character in stream:
        message.append(ord(character))
    return message

def integer_list_to_asci(integer_list):
    message = ""
    for integer in integer_list:
        message = message + str(chr(integer))
    return message

def main(args):
    # Initialise Chirp SDK
    sdk = ChirpSDK()
    print(str(sdk))
    print('Protocol: {protocol} [v{version}]'.format(
        protocol=sdk.protocol_name,
        version=sdk.protocol_version))
    print(sdk.audio.query_devices())

    # Configure audio
    sdk.audio.input_device = args.i
    sdk.audio.output_device = args.o
    sdk.audio.block_size = args.b
    sdk.input_sample_rate = args.s
    sdk.output_sample_rate = args.s

    # Set callback functions
    sdk.set_callbacks(Callbacks())

    # Generate random payload and send
    #payload = sdk.random_payload()
    sdk.start(send=True, receive=True)
    if args.send:
        print("Processing payload")
        integer_list = asci_to_integer_list(args.send)
        if len(integer_list) <= 8:
            sdk.send(integer_list)
        else:
            max_size_list = []
            index = 1
            try:
                len(integer_list) % 8
            except:
                raise Exception("Message must be devisible by 8")

            max_size_list = []
            for integer in integer_list:
                if len(max_size_list) == 8:
                    if index <= 9:
                        pass
                    else:
                        time.sleep(WAIT_TIME)
                    sdk.send(max_size_list)
                    print("Send text: {}".format(integer_list_to_asci(max_size_list)))
                    max_size_list = []
                else:
                    max_size_list.append(integer)
                index = index + 1

    try:
        # Process audio streams
        while True:
            time.sleep(0.1)
#            sys.stdout.write('.')
            sys.stdout.flush()
    except KeyboardInterrupt:
        print('Exiting')

    sdk.stop()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='ChirpSDK Demo',
        epilog='Sends a random chirp payload, then continuously listens for chirps'
    )
    parser.add_argument('-c', default='~/.chirprc', help='The configuration block [name] in your ~/.chirprc file (optional)')
    parser.add_argument('-i', type=int, default=None, help='Input device index (optional)')
    parser.add_argument('-o', type=int, default=None, help='Output device index (optional)')
    parser.add_argument('-b', type=int, default=0, help='Block size (optional)')
    parser.add_argument('-s', type=int, default=44100, help='Sample rate (optional)')
    parser.add_argument('-mode', default=False, help='Enable full or half mode')
    parser.add_argument('-send', default=False, help='Message to send')

    args = parser.parse_args()
    print(args)

    main(args)
