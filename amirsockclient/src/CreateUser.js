import React, { useState, Fragment } from "react";
import Axios from "axios";
import {withRouter} from "react-router-dom"

const CreateUser = (props) => {
  const [name, setName] = useState("");
  const [username, SetUserName] = useState("");
  const [password, setPassword] = useState("");

  var sendTheUser = async () => {
    let content = {
      name: name,
      username: username,
      password: password,
    };
    const resp = await Axios.post("/restchat/createuser", content);
    console.log(resp.data);
  };

  return (
    <Fragment>
      <form>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="name"
        />
        <input
          value={username}
          onChange={(e) => SetUserName(e.target.value)}
          placeholder="username"
        />
        <input
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="password"
        />
        <button type="button" onClick={sendTheUser}>
          create the user
        </button>
      </form>
    </Fragment>
  );
};

export default withRouter(CreateUser);
