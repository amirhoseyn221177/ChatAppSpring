import React, { useState, Fragment } from "react";
import Axios from "axios";
import { withRouter } from "react-router-dom";

const CreateGroupChat = (props) => {
  const [name, setName] = useState("");
  const [founder, setFounder] = useState("");

  var creatTheGroup = async () => {
    let content = {
      username: founder,
    };
    const rep = await Axios.post(`http://localhost:8080/restchat/create/${name}`, content);
    console.log(rep.data);
  };

  return (
    <Fragment>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Groupname"
      />
      <input
        value={founder}
        onChange={(e) => setFounder(e.target.value)}
        placeholder="username"
      />
      <button onClick={creatTheGroup}>Create the Group Chat</button>
    </Fragment>
  );
};

export default withRouter (CreateGroupChat);
