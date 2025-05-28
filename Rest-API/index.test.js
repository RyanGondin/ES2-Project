/* eslint-env jest */
import { requestLogger } from "./middleware";
import { jest } from "@jest/globals";

const fakeDB = {};
const user = { id: "Martim", username: "Martim" };

const api = {
  createApp: ({ body, user }) => {
    const { name, owner, editors, viewers } = body;
    if (!name || !owner) {
      return {
        status: 400,
        json: { error: "App name and owner are required" },
      };
    }
    const appid = `app${Object.keys(fakeDB).length + 1}`;
    fakeDB[appid] = {
      name,
      owner,
      editors: Array.isArray(editors) ? editors : [],
      viewers: Array.isArray(viewers) ? viewers : [],
      ...(body.password && { password: body.password }),
    };
    return {
      status: 201,
      json: {
        appid,
        name,
        owner,
        editors: fakeDB[appid].editors,
        viewers: fakeDB[appid].viewers,
      },
    };
  },

  listApps: ({ user }) => {
    const apps = Object.entries(fakeDB)
      .filter(
        ([_, rel]) =>
          rel.owner === user.id ||
          rel.editors.includes(user.id) ||
          rel.viewers.includes(user.id)
      )
      .map(([appid, rel]) => ({
        appid,
        owner: rel.owner,
        role:
          rel.owner === user.id
            ? "owner"
            : rel.editors.includes(user.id)
            ? "editor"
            : "viewer",
      }));
    return { status: 200, json: apps };
  },

  postPassword: (req) => {
    const app = fakeDB[req.params.appid];
    if (!app) return { status: 404, json: { error: "App not found" } };
    if (!req.body.password)
      return { status: 400, json: { error: "Password is required" } };

    // Simula autorização
    if (!["owner", "editor"].includes(req.user.role)) {
      return { status: 403, json: { error: "Forbidden" } };
    }

    app.password = req.body.password;
    return { status: 201, json: { message: "Password criada com sucesso" } };
  },

  putPassword: (req) => {
    const app = fakeDB[req.params.appid];
    if (!app) return { status: 404, json: { error: "App not found" } };
    if (!req.body.password)
      return { status: 400, json: { error: "Password is required" } };

    if (!["owner", "editor"].includes(req.user.role)) {
      return { status: 403, json: { error: "Forbidden" } };
    }

    app.password = req.body.password;
    return {
      status: 200,
      json: { message: "Password atualizada com sucesso" },
    };
  },

  getPassword: (req) => {
    const app = fakeDB[req.params.appid];
    if (!app) return { status: 404, json: { error: "Password not found" } };

    if (!["owner", "editor", "viewer"].includes(req.user.role)) {
      return { status: 403, json: { error: "Forbidden" } };
    }

    return { status: 200, json: { password: app.password } };
  },
};

describe("API Whitebox - Apps e Passwords", () => {
  beforeEach(() => {
    // Limpar a base de dados simulada antes de cada teste
    for (const k in fakeDB) delete fakeDB[k];
  });

  test("Criar app sem nome retorna 400", () => {
    const res = api.createApp({ body: {}, user });
    expect(res.status).toBe(400);
    expect(res.json.error).toBe("App name and owner are required");
  });

  test("Criar app com nome funciona", () => {
    const res = api.createApp({
      body: { name: "MinhaApp", owner: "Martim", password: "123" },
      user,
    });
    expect(res.status).toBe(201);
    expect(res.json).toHaveProperty("appid");
    expect(fakeDB[res.json.appid].owner).toBe(user.id);
  });

  test("Criar app com mesmo nome não impede criação", () => {
    const res1 = api.createApp({
      body: { name: "Repetida", owner: "user1" },
      user,
    });
    const res2 = api.createApp({
      body: { name: "Repetida", owner: "user1" },
      user,
    });
    expect(res1.status).toBe(201);
    expect(res2.status).toBe(201);
    expect(res1.json.appid).not.toBe(res2.json.appid);
  });

  test("Listar apps retorna array vazio se não houver apps", () => {
    const res = api.listApps({ user });
    expect(res.status).toBe(200);
    expect(Array.isArray(res.json)).toBe(true);
    expect(res.json.length).toBe(0);
  });

  test("Listar apps retorna array", () => {
    api.createApp({ body: { name: "MinhaApp", owner: "user1" }, user });
    const res = api.listApps({ user });
    expect(res.status).toBe(200);
    expect(Array.isArray(res.json)).toBe(true);
  });

  test("POST password falha sem password", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", owner: "user1" },
      user,
    }).json;
    const req = {
      params: { appid },
      body: {},
      user: { ...user, role: "owner" },
    };
    const res = api.postPassword(req);
    expect(res.status).toBe(400);
    expect(res.json.error).toMatch(/Password is required/);
  });

  test("POST password falha para não autorizado", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", owner: "user1" },
      user,
    }).json;
    const req = {
      params: { appid },
      body: { password: "abc" },
      user: { ...user, role: "viewer" },
    };
    const res = api.postPassword(req);
    expect(res.status).toBe(403);
  });

  test("POST password falha para app inexistente", () => {
    const req = {
      params: { appid: "naoexiste" },
      body: { password: "abc" },
      user: { ...user, role: "owner" },
    };
    const res = api.postPassword(req);
    expect(res.status).toBe(404);
    expect(res.json.error).toMatch(/App not found/);
  });

  test("POST password funciona para owner/editor", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", owner: "user1" },
      user,
    }).json;
    ["owner", "editor"].forEach((role) => {
      const req = {
        params: { appid },
        body: { password: "abc123" },
        user: { ...user, role },
      };
      const res = api.postPassword(req);
      expect(res.status).toBe(201);
      expect(fakeDB[appid].password).toBe("abc123");
    });
  });

  test("GET password falha se não autorizado", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", password: "pw", owner: "Martim" },
      user,
    }).json;
    const req = { params: { appid }, user: { ...user, role: "guest" } };
    const res = api.getPassword(req);
    expect(res.status).toBe(403);
  });

  test("GET password falha para app inexistente", () => {
    const req = {
      params: { appid: "naoexiste" },
      user: { ...user, role: "owner" },
    };
    const res = api.getPassword(req);
    expect(res.status).toBe(404);
    expect(res.json.error).toMatch(/Password not found/);
  });

  test("GET password funciona para owner/editor/viewer", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", password: "pw", owner: "Martim" },
      user,
    }).json;
    ["owner", "editor", "viewer"].forEach((role) => {
      const req = { params: { appid }, user: { ...user, role } };
      const res = api.getPassword(req);
      expect(res.status).toBe(200);
      expect(res.json).toHaveProperty("password");
      expect(res.json.password).toBe("pw");
    });
  });

  test("Password pode ser sobrescrita", () => {
    const { appid } = api.createApp({
      body: { name: "MinhaApp", password: "pw", owner: "user1" },
      user,
    }).json;
    const req = {
      params: { appid },
      body: { password: "nova" },
      user: { ...user, role: "owner" },
    };
    const res = api.putPassword(req);
    expect(res.status).toBe(200);
    expect(fakeDB[appid].password).toBe("nova");
  });
});

describe("Mock do Request Logger", () => {
  let consoleSpy;

  beforeEach(() => {
    // Espionar o console.log
    consoleSpy = jest.spyOn(console, "log").mockImplementation(() => {});
  });

  afterEach(() => {
    // Restaurar o comportamento original do console.log após cada teste
    consoleSpy.mockRestore();
  });

  test("should log request info with user id and status code", () => {
    const req = {
      method: "GET",
      originalUrl: "/apps/123/password",
      user: { id: "user1" },
    };

    const res = {}; // Simulação de resposta vazia
    const next = jest.fn(); // Simulação da função next

    // Chama a função requestLogger diretamente
    requestLogger(req, res, next);

    // Verifica se o console.log foi chamado
    expect(consoleSpy).toHaveBeenCalled();

    // Verifica se o formato do log está correto
    const logMessage = consoleSpy.mock.calls[0][0];
    const expectedPattern =
      /^\[\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z\] GET \/apps\/123\/password by user1$/;
    expect(logMessage).toMatch(expectedPattern);

    // Verifica se a função next foi chamada (o middleware deve passar a requisição)
    expect(next).toHaveBeenCalled();
  });
});
