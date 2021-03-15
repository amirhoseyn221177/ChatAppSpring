import React, { useState, Fragment, useEffect, useMemo } from "react";
import { withRouter } from "react-router-dom";
import { connect } from 'react-redux'


var PrivateMessage = (props) => {
  const [user, setUser] = useState(props.match.params.username);
  const [otherUser, setOtheruser] = useState(props.match.params.otheruser);
  const [value, setValue] = useState("");
  const [socket, setSocket] = useState(null)
  useEffect(() => {
    let socket = new WebSocket(`ws://10.0.0.8:8080/ws/${user}`)
    setSocket(socket)
    //eslint-disable-next-line
  }, [])


    if (socket !== null) {
      socket.onmessage = (e) => {
        console.log(e)

      }
      socket.onopen = () => {
        var chatMessage = {
          textContent: "we are connected and authorized",
          sender: user,
          receiver:otherUser,
          mediaContent: null,
          contentType: null,
          token: "bearer " + localStorage.getItem(`token for ${user}`)
        };

        socket.send(JSON.stringify(chatMessage))
      }
    }else{
      console.log("socket is null")
    }





    // if (socket !== null) {
    //   console.log(24)
    //   socket.onmessage = (e) => {
    //     console.log(e)

    //   }
    //   socket.onopen = () => {
    //     var chatMessage = {
    //       textContent: "we are connected and authorized",
    //       sender: user,
    //       receiver:otherUser,
    //       mediaContent: null,
    //       contentType: null,
    //       token: "bearer " + localStorage.getItem(`token for ${user}`)
    //     };

    //     socket.send(JSON.stringify(chatMessage))
    //   }
    // }else{
    //   console.log("socket is null")
    // }



  var SendingMessages = () => {
      var chatMessage = {
      textContent: value,
      sender: user,
      receiver: otherUser,
      mediaContent: null,
      contentType: null,
      token: "bearer " + localStorage.getItem(`token for ${user}`)
    };
    socket.send(JSON.stringify(chatMessage))
  }


  return (
    <Fragment>
      <div>
        <input
          placeholder="username"
          value={user}
          onChange={(e) => setUser(e.target.value)}
        />
        <input
          placeholder="otherUser"
          value={otherUser}
          onChange={(e) => setOtheruser(e.target.value)}
        />

        <form>
          <h2>private message</h2>
          <input
            placeholder="type"
            value={value}
            onChange={(e) => setValue(e.target.value)}
          />
          <button type="button" onClick={SendingMessages}>
            Send
          </button>

          {/* <input type='file' name='image' accept="image/*" onChange={e => gettingFile(e)} /> */}
          {/* <button type="button" onClick={diconnecting}>disconnect</button> */}
        </form>
        {/* <div>{broadCastMessage}</div> */}
      </div>
    </Fragment>
  );
};



const maptoState = state => {
  return {
    auth: state.auth.token
  }
}

export default withRouter(connect(maptoState)(PrivateMessage));
