import React, { useState, useEffect } from "react";
import Stomp from "stompjs";
import Sockjs from "sockjs-client";
var stompClient = null;

const PublicMessage = (props) => {
  const [broadCastMessage, setBroadCastMessage] = useState([]);
  const [openMessageBox, setOpenMessageBox] = useState(false);
  const [channelConnected, setChannelConnected] = useState(false);
  const [message,setMessage]=useState([])
  const [value,setValue]=useState("")



  var connect = async(username) => {
    if (username) {
     var sockjs = new Sockjs("/ws");
      stompClient = Stomp.over(sockjs);
     stompClient.connect({}, onConnected, onError);
      console.log(20)
    }
  };


  var onConnected = () => {
      console.log(25)
    setChannelConnected(true);
    stompClient.subscribe("/topic/public",onMessageReceived)
    stompClient.send("/app/addUser", {}, JSON.stringify({ sender:"amir", type: 'JOIN' }))


  };

  var onError=()=>{
      console.error("error")
  }

  var sendMessage=()=>{
      console.log(value)
      let chatMassege={
          sender:"amir sayyar",
          content:value,
          type:"CHAT",
          receiver:"sepehr",
      }
      stompClient.send("/app/sendMessage",{},JSON.stringify(chatMassege))
  }


  var onMessageReceived=(payload)=>{
      var message=JSON.parse(payload.body)
      console.log(52)
      console.log(message)
      setBroadCastMessage(prev=>[...prev,message.content])


  }

  return(
      <>
      <div>
          <h2>messages</h2>
      </div>
      <form>
          <input type="text" value={value} onChange={(e)=>setValue(e.target.value)}/>
        
          <button type="button" onClick={sendMessage}>Send</button>
          <button type="button" onClick={connect}>connect</button>
      </form>
      <div>
      {
              broadCastMessage.map(x => <p>{x}</p>)
          }
      </div>


      </>
     
  )
};

export default PublicMessage;
