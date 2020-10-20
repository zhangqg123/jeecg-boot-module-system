package org.jeecg.modules.qwert.jst.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.conn.modbus4j.ModbusUtil;
import org.jeecg.modules.conn.snmp.SnmpData;
import org.jeecg.modules.conn.snmp.SnmpTest;
import org.jeecg.modules.dbserver.mongo.common.model.Alarm;
import org.jeecg.modules.dbserver.mongo.common.model.Audit;
import org.jeecg.modules.dbserver.mongo.repository.impl.DemoRepository;
import org.jeecg.modules.qwert.jst.entity.JstZcAlarm;
import org.jeecg.modules.qwert.jst.entity.JstZcCat;
import org.jeecg.modules.qwert.jst.entity.JstZcDev;
import org.jeecg.modules.qwert.jst.entity.JstZcRev;
import org.jeecg.modules.qwert.jst.entity.JstZcTarget;
import org.jeecg.modules.qwert.jst.service.IJstZcAlarmService;
import org.jeecg.modules.qwert.jst.service.IJstZcCatService;
import org.jeecg.modules.qwert.jst.service.IJstZcDevService;
import org.jeecg.modules.qwert.jst.service.IJstZcTargetService;
import org.jeecg.modules.qwert.jst.utils.JstConstant;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;

import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: jst_zc_dev
 * @Author: jeecg-boot
 * @Date: 2020-07-24
 * @Version: V1.0
 */
@Api(tags = "jst_zc_dev")
@RestController
@RequestMapping("/jst/jstZcDev")
@Slf4j
public class JstZcDevController extends JeecgController<JstZcDev, IJstZcDevService> {
	@Autowired
	private IJstZcCatService jstZcCatService;
	@Autowired
	private IJstZcDevService jstZcDevService;
	@Autowired
	private IJstZcTargetService jstZcTargetService;
	@Autowired
	private IJstZcAlarmService jstZcAlarmService;
    @Autowired
    DemoRepository repository;
    
    private List<JstZcCat> jzcList;
    private List<JstZcDev> jzdList;    
    private List<JstZcTarget> jztList;
//	private boolean runflag = true;

	/**
	 * 分页列表查询
	 *
	 * @param jstZcDev
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-分页列表查询")
	@ApiOperation(value = "jst_zc_dev-分页列表查询", notes = "jst_zc_dev-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(JstZcDev jstZcDev, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		Map<String, String[]> aaa = req.getParameterMap();
		QueryWrapper<JstZcDev> queryWrapper = QueryGenerator.initQueryWrapper(jstZcDev, req.getParameterMap());
		queryWrapper.orderByAsc("dev_cat");
		queryWrapper.orderByAsc("dev_no");
		Page<JstZcDev> page = new Page<JstZcDev>(pageNo, pageSize);
		IPage<JstZcDev> pageList = jstZcDevService.page(page, queryWrapper);
		return Result.ok(pageList);
	}

	/**
	 * 添加
	 *
	 * @param jstZcDev
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-添加")
	@ApiOperation(value = "jst_zc_dev-添加", notes = "jst_zc_dev-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody JstZcDev jstZcDev) {
		jstZcDevService.save(jstZcDev);
		return Result.ok("添加成功！");
	}

	/**
	 * 测试
	 *
	 * @param jstZcDev
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-测试")
	@ApiOperation(value = "jst_zc_dev-测试", notes = "jst_zc_dev-测试")
	@PostMapping(value = "/conntest")
	public Result<?> conntest(@RequestBody JstZcRev jstZcRev) {
		String devNo=jstZcRev.getDevNo();
		String conInfo = jstZcRev.getConnInfo();
		String revList = jstZcRev.getRevList();
		List<JstZcTarget> jztList = null;
		List resList = new ArrayList();
		if (jstZcRev.getDevType() != null) {
			JstZcTarget jstZcTarget = new JstZcTarget();
			jstZcTarget.setDevType(jstZcRev.getDevType());
//			jstZcTarget.setCtrlDown("");
			QueryWrapper<JstZcTarget> queryWrapper = QueryGenerator.initQueryWrapper(jstZcTarget, null);
			queryWrapper.orderByAsc("instruct");
			queryWrapper.orderByAsc("tmp");
			jztList = jstZcTargetService.list(queryWrapper);
		} else {
			jztList = JSONArray.parseArray(revList, JstZcTarget.class);
		}
		JSONObject jsonConInfo = JSON.parseObject(conInfo);
		String ipAddress = jsonConInfo.getString("ipAddress");
		String port = jsonConInfo.getString("port");
		String type = jsonConInfo.getString("type");
		String retry = jsonConInfo.getString("retry");

		String slave = null;

		String version = null;
		String timeOut = null;
		String community = null;
		BatchResults<String> results = null;
		JstZcTarget jztl = jztList.get(jztList.size() - 1);
		boolean extype = (jztl.getDevType().equals("kangmingsipcc3300") || jstZcRev.getDevType() == null);
		if (type.equals("SOCKET")) {
			int ts = Integer.parseInt(jztl.getAddress());
			slave = jsonConInfo.getString("slave");
			timeOut = jsonConInfo.getString("sotimeout");

			IpParameters ipParameters = new IpParameters();
			ipParameters.setHost(ipAddress);
			ipParameters.setPort(Integer.parseInt(port));
			ipParameters.setEncapsulated(true);

			ModbusFactory modbusFactory = new ModbusFactory();
			ModbusMaster master = modbusFactory.createTcpMaster(ipParameters, false);

//			jztList.sort((x, y) -> Integer.compare(Integer.parseInt(x.getAddress()), Integer.parseInt(y.getAddress())));
			boolean flag = false;
			try {
				if (ts > 200) {
					master.setTimeout(3000);
				}
				master.init();
				int slaveId = 0;
//				if (isNumeric(slave)) {
//					slaveId = Integer.parseInt(slave);
//				} else {
					BigInteger slavebigint = new BigInteger(slave, 16);
					slaveId = slavebigint.intValue();
//				}
				BatchRead<String> batch = new BatchRead<String>();
				String tmpInstruct = null;
				int tmpOffset = 0;
				int tmp2Offset = 0;
				boolean batchSend = false;
				int revnull=0;
				for (int i = 0; i < jztList.size(); i++) {

					JstZcTarget jzt = jztList.get(i);
					String targetNo = jzt.getTargetNo();
					String di = jzt.getInstruct().substring(0, 2);
					String oshexs = jzt.getInstruct().substring(2, 6);
					String lenhexs = jzt.getInstruct().substring(6, 10);
					String instruct = jzt.getInstruct();
					
					int offset = 0;
					int len = 0;
					if (extype) {
						if(jzt.getDevType().equals("kangmingsipcc3300")) {
							BigInteger bigint = new BigInteger(oshexs, 16);
							offset = bigint.intValue();
							offset = offset + Integer.parseInt(jzt.getOffset())/2;
							len=(Integer.valueOf(jzt.getLen()))/2;
						}else {
							if (jzt.getAddress() != null) {
								offset = offset + Integer.parseInt(jzt.getAddress());
							}
						}
						tmpInstruct = instruct;

					} else {

						if (jzt.getAddress() != null) {
							offset = offset + Integer.parseInt(jzt.getAddress());
						}
						if (instruct.equals(tmpInstruct) && offset==tmp2Offset) {
							continue;
						}
						if (ts > 200 && offset - tmpOffset >= 80) {
							flag = true;
						}
						tmpInstruct = instruct;
						tmp2Offset=offset;
						if (flag == true) {
							System.out.println(i + "::" + offset);
							results = master.send(batch);
							resList.add(results.toString());
							if(results.toString().equals("{}")) {
								revnull=revnull+1;
								if(revnull>2) {
									System.out.println(devNo+"::通讯中断");
									break;
								}
							}
							System.out.println(results);
							batch = new BatchRead<String>();
							flag = false;
							tmpOffset = offset;
						}
					}
					String res = null;
					Map<String, String> resMap = new HashMap<String, String>();

					if (di.equals("04")) {
						if (extype) {
					//		res = ModbusUtil.readInputRegistersTest(master, slaveId, offset, len);
							batch = new BatchRead<String>();
							batch.addLocator(jzt.getId(),
									BaseLocator.inputRegister(slaveId, offset, Integer.parseInt(jzt.getDataType())));
							results = master.send(batch);
							System.out.println(devNo+"::"+targetNo+"::"+tmpInstruct+"::"+results);
							res=results.toString();
							if(res.equals("{}")) {
								break;
							}
							res=res.substring(1,res.length()-1);
							String[] r1 = res.split("=");
							resMap.put("devNo", devNo);
							resMap.put("targetNo", targetNo);
							resMap.put("instruct", tmpInstruct);
							resMap.put("resData", "["+r1[1]+"]");
							resList.add(resMap);
						} else {
							batch.addLocator(jzt.getId(),
									BaseLocator.inputRegister(slaveId, offset, Integer.parseInt(jzt.getDataType())));
							batchSend = true;
						}
					}
					if (di.equals("03")) {
						if (extype) {
					//		res = ModbusUtil.readHoldingRegistersTest(master, slaveId, offset, len);
							batch = new BatchRead<String>();
							batch.addLocator(jzt.getId(),
									BaseLocator.holdingRegister(slaveId, offset, Integer.parseInt(jzt.getDataType())));
							results = master.send(batch);
							System.out.println(devNo+"::"+targetNo+"::"+tmpInstruct+"::"+results);
							res=results.toString();
							if(res.equals("{}")) {
								break;
							}
							res=res.substring(1,res.length()-1);
							String[] r1 = res.split("=");
							resMap.put("devNo", devNo);
							resMap.put("targetNo", targetNo);
							resMap.put("instruct", tmpInstruct);
							resMap.put("resData", "["+r1[1]+"]");
							resList.add(resMap);
						} else {
							batch.addLocator(jzt.getId(),
									BaseLocator.holdingRegister(slaveId, offset, Integer.parseInt(jzt.getDataType())));
							batchSend = true;
						}
					}
					if (di.equals("02")) {
						if (extype) {
							ModbusUtil.readDiscreteInputTest(master, slaveId, offset, len);
						} else {
							batch.addLocator(jzt.getId(), BaseLocator.inputStatus(slaveId, offset));
							batchSend = true;
						}
					}
				}
				if (batchSend == true) {
					results = master.send(batch);
					resList.add(results.toString());
					System.out.println(devNo+"::"+results);
				}
//				System.out.println(devNo+"::"+results);
				System.out.println("resList.size:" + resList.size());
				
			} catch (ModbusInitException e) {
				e.printStackTrace();
			} catch (ModbusTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrorResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				master.destroy();
			}
		}
		if (type.equals("SNMP")) {
			version = jsonConInfo.getString("version");
			timeOut = jsonConInfo.getString("timeOut");
			community = jsonConInfo.getString("community");
			jztList.stream().sorted(Comparator.comparing(JstZcTarget::getInstruct));
			List<String> oidList = new ArrayList<String>();
			for (int i = 0; i < jztList.size(); i++) {
				JstZcTarget jzt = jztList.get(i);
				String oidval = jzt.getInstruct();
				System.out.println(devNo+"::");
				List snmpList = SnmpData.snmpGet(ipAddress, community, oidval,null);
				if(snmpList.size()>0) {
					for(int j=0;j<snmpList.size();j++) {
						resList.add(snmpList.get(j));
					}
				}else {
					break;
				}
			}
		}
		return Result.ok(resList);
	}
	
	@AutoLog(value = "jst_zc_dev-读取")
	@ApiOperation(value = "jst_zc_dev-读取", notes = "jst_zc_dev-读取")
	@GetMapping(value = "/readClose")
	public Result<?> readClose(HttpServletRequest req) {
		JstConstant.runflag=false;
		return Result.ok("ok");
	}
	
	@AutoLog(value = "jst_zc_dev-读取")
	@ApiOperation(value = "jst_zc_dev-读取", notes = "jst_zc_dev-读取")
	@GetMapping(value = "/handleRead")
	public Result<?> handleRead(HttpServletRequest req) {
//		long start, end;
//		start = System.currentTimeMillis();
		boolean allflag = true;
		JstConstant.runflag=true;
		String catNo = req.getParameter("devCat");
//		QueryWrapper<JstZcCat> cqw = QueryGenerator.initQueryWrapper(new JstZcCat(), null);
//		cqw.eq("has_child", "0");
//		cqw.eq("origin_id",catNo);
		jzcList = jstZcCatService.queryJzcList();
		
//		QueryWrapper<JstZcDev> dqw = QueryGenerator.initQueryWrapper(new JstZcDev(), null);
//		dqw.eq("status", "0");
//		dqw.orderByAsc("dev_no");
		jzdList = jstZcDevService.queryJzdList();

//		JstZcTarget jstZcTarget = new JstZcTarget();
//		jstZcTarget.setDevType(jstZcCat.getOriginId());
//		QueryWrapper<JstZcTarget> zqw = QueryGenerator.initQueryWrapper(new JstZcTarget(), null);
//		zqw.orderByAsc("instruct");
//		zqw.orderByAsc("tmp");
		jztList = jstZcTargetService.queryJztList();			
		
        MyThread mt=new MyThread(allflag,catNo);
        new Thread(mt).start();
//		threadWork(allflag, catNo);
//		end = System.currentTimeMillis();
//		System.out.println("开始时间:" + start + "; 结束时间:" + end + "; 用时:" + (end - start) + "(ms)");
		return Result.ok("ok");
	}

	public void threadWork(boolean allflag, String catNo) {
		if(catNo==null||catNo=="") {
			return;
		}
		List<JstZcCat> jzcCollect = null;
		while(allflag&&JstConstant.runflag) {
			if(!catNo.equals("all")) {
				allflag=false;
		        jzcCollect = jzcList.stream().filter(u -> catNo.equals(u.getOriginId())).collect(Collectors.toList());
			}else {
				jzcCollect=jzcList;
			}
			for (int i = 0; i < jzcCollect.size(); i++) {
				if(!JstConstant.runflag) {
					break;
				}
				JstZcCat jstZcCat = jzcCollect.get(i);
//				JstZcDev jstZcDev = new JstZcDev();
//				jstZcDev.setDevCat(jstZcCat.getOriginId());
				
//				jstZcDev.setStatus(0);
//				QueryWrapper<JstZcDev> dqw = QueryGenerator.initQueryWrapper(jstZcDev, null);
//				dqw.orderByAsc("dev_no");
//				List<JstZcDev> jzdList = jstZcDevService.list(dqw);
		        List<JstZcDev> jzdCollect = jzdList.stream().filter(u -> jstZcCat.getOriginId().equals(u.getDevCat())).collect(Collectors.toList());
				
//				JstZcTarget jstZcTarget = new JstZcTarget();
//				jstZcTarget.setDevType(jstZcCat.getOriginId());
//				QueryWrapper<JstZcTarget> zqw = QueryGenerator.initQueryWrapper(jstZcTarget, null);
//				zqw.orderByAsc("instruct");
//				zqw.orderByAsc("tmp");
//				List<JstZcTarget> jztList = jstZcTargetService.list(zqw);			

				List<JstZcTarget> jztCollect = jztList.stream().filter(u -> jstZcCat.getOriginId().equals(u.getDevType())).collect(Collectors.toList());

				targetRead(jzdCollect,jztCollect);
			}
		}
	}

	/**
	 * 测试
	 * 
	 * @param devCat
	 *
	 * @param jstZcDev
	 * @return
	 */

	public Result<?> targetRead(List<JstZcDev> jzdCollect,List<JstZcTarget> jztCollect) {

		for (int i = 0; i < jzdCollect.size(); i++) {
			if(!JstConstant.runflag) {
				break;
			}
			List resList = new ArrayList();
			JstZcDev jzd = jzdCollect.get(i);
			String devNo=jzd.getDevNo();
			String devName=jzd.getDevName();
			String catNo = jzd.getDevCat();
			String conInfo = jzd.getConInfo();
//			System.out.println(conInfo);
			JSONObject jsonConInfo = JSON.parseObject(conInfo);
			String ipAddress = jsonConInfo.getString("ipAddress");
			String port = jsonConInfo.getString("port");
			String type = jsonConInfo.getString("type");
			String retry = jsonConInfo.getString("retry");
			String slave = null;

			String version = null;
			String timeOut = null;
			String community = null;

			BatchResults<String> results = null;
			JstZcTarget jztl = jztCollect.get(jztCollect.size() - 1);
			boolean extype = (jzd.getDevNo().equals("kangminbgsiPC330"));
//			boolean extype=true;
			if (type.equals("SOCKET")) {
				int ts = Integer.parseInt(jztl.getAddress());
				slave = jsonConInfo.getString("slave");
				timeOut = jsonConInfo.getString("sotimeout");

				IpParameters ipParameters = new IpParameters();
				ipParameters.setHost(ipAddress);
				ipParameters.setPort(Integer.parseInt(port));
				ipParameters.setEncapsulated(true);

				ModbusFactory modbusFactory = new ModbusFactory();
				ModbusMaster master = modbusFactory.createTcpMaster(ipParameters, false);

//				jztList.sort((x, y) -> Integer.compare(Integer.parseInt(x.getAddress()), Integer.parseInt(y.getAddress())));
				boolean flag = false;
				try {
					if(i%5==4) {
						Thread.sleep(100);
					}

					if (ts > 200) {
						master.setTimeout(3000);
					}
					master.init();
					int slaveId = 0;
//					if (isNumeric(slave)) {
//						slaveId = Integer.parseInt(slave);
//					} else {
						BigInteger slavebigint = new BigInteger(slave, 16);
						slaveId = slavebigint.intValue();
//					}
					BatchRead<String> batch = new BatchRead<String>();
					String tmpInstruct = null;
					int tmpOffset = 0;
					int tmp2Offset=0;
					boolean batchSend = false;
					int revnull=0;
					if(jztCollect.size()>0) {
						boolean alarmFlag = false;
						for (int j = 0; j < jztCollect.size(); j++) {
	
							JstZcTarget jzt = jztCollect.get(j);
	
							String di = jzt.getInstruct().substring(0, 2);
							String oshexs = jzt.getInstruct().substring(2, 6);
							String lenhexs = jzt.getInstruct().substring(6, 10);
							String instruct = jzt.getInstruct();
							int offset = 0;
							int len = 0;
							if (extype) {
								if (instruct.equals(tmpInstruct)) {
									continue;
								}
								tmpInstruct = instruct;
								BigInteger bigint = new BigInteger(oshexs, 16);
								offset = bigint.intValue();
	
								tmpOffset = offset;
								bigint = new BigInteger(lenhexs, 16);
								len = bigint.intValue();
							} else {
	
								if (jzt.getAddress() != null) {
									offset = offset + Integer.parseInt(jzt.getAddress());
								}
	
								if (instruct.equals(tmpInstruct) && offset==tmp2Offset) {
									continue;
								}
								if (ts > 200 && offset - tmpOffset >= 80) {
									flag = true;
								}
								tmpInstruct = instruct;
								tmp2Offset=offset;
	
								if (flag == true) {
		//							System.out.println(jzd.getDevNo()+"::"+j + "::" + offset);
									if(tmpOffset>0) {
										results = master.send(batch);
										resList.add(results.toString());
										if(results.toString().equals("{}")) {
											revnull=revnull+1;
											if(revnull>0) {
												JstZcAlarm jstZcAlarm = new JstZcAlarm();
												jstZcAlarm.setDevNo(devNo);
												jstZcAlarm.setDevName(devName);
												jstZcAlarm.setCatNo(catNo);
												jstZcAlarm.setTargetNo("connection-fail");
												jstZcAlarm.setSendTime(new Date());
												jstZcAlarm.setSendType("0");
												jstZcAlarmService.saveSys(jstZcAlarm);
												alarmFlag=true;
												System.out.println(devNo+"::通讯中断,发出报警");
												break;
											}
										}
									}
									System.out.println(devNo+"::"+results);
									batch = new BatchRead<String>();
									flag = false;
									tmpOffset = offset;
								}
							}
							String res = null;
							Map<String, String> resMap = new HashMap<String, String>();
	
							if (di.equals("04")) {
				//				System.out.println(jzd.getDevNo());
	
								if (extype) {
									res = ModbusUtil.readInputRegistersTest(master, slaveId, offset, len);
									System.out.println(devNo+"::"+tmpInstruct+"::"+res);
									if(res.equals("devicefail")) {
										break;
									}
									resMap.put("instruct", tmpInstruct);
									resMap.put("resData", res);
									resList.add(resMap);
								} else {
									batch.addLocator(jzt.getId(), BaseLocator.inputRegister(slaveId, offset,
											Integer.parseInt(jzt.getDataType())));
									batchSend = true;
								}
							}
							if (di.equals("03")) {
				//				System.out.println(jzd.getDevNo());
								if (extype) {
									res = ModbusUtil.readHoldingRegistersTest(master, slaveId, offset, len);
									System.out.println(devNo+"::"+tmpInstruct+"::"+res);
									if(res.equals("devicefail")) {
										break;
									}
									resMap.put("instruct", tmpInstruct);
									resMap.put("resData", res);
									resList.add(resMap);
								} else {
									batch.addLocator(jzt.getId(), BaseLocator.holdingRegister(slaveId, offset,
											Integer.parseInt(jzt.getDataType())));
									batchSend = true;
								}
							}
							if (di.equals("02")) {
				//				System.out.println(jzd.getDevNo());
								if (extype) {
									ModbusUtil.readDiscreteInputTest(master, slaveId, offset, len);
								} else {
									batch.addLocator(jzt.getId(), BaseLocator.inputStatus(slaveId, offset));
									batchSend = true;
								}
							}
	
						}
						if (batchSend == true && alarmFlag==false) {
						//	for(int n=0;n<3;n++) {
								results = master.send(batch);
								if(results.toString().equals("{}")) {
								//	Thread.sleep(500);
								//	results=master.send(batch);
								//	if(results.toString().equals("{}")) {
										JstZcAlarm jstZcAlarm = new JstZcAlarm();
										jstZcAlarm.setDevNo(devNo);
										jstZcAlarm.setDevName(devName);
										jstZcAlarm.setCatNo(catNo);
										jstZcAlarm.setTargetNo("connection-fail");
										jstZcAlarm.setSendTime(new Date());
										jstZcAlarm.setSendType("0");
										jstZcAlarmService.saveSys(jstZcAlarm);
										System.out.println(devNo+"::通讯中断,发出报警");
							//			break;
								//	}
								}
						//	    Thread.currentThread().sleep(500);
						//	}
							resList.add(results.toString());
							System.out.println(devNo+"::"+results);
						}
	//					System.out.println(results);
						System.out.println(devNo+"::resList.size::"+resList.size());
						String alarmValue="";
						String alarmNo="";
						for(int ri=0;ri<resList.size();ri++) {
							String r1=(String) resList.get(ri);
							r1=r1.replaceAll(" ", "");
							r1=r1.substring(1, r1.length()-1);
							String[] r2 = r1.split(",");
							for(int rj=0;rj<r2.length;rj++) {
								String[] r3 = r2[rj].split("=");
								for(int rk=0;rk<jztCollect.size();rk++) {
									JstZcTarget jzt = jztCollect.get(rk);
									if(jzt.getAlarmPoint().equals("1")&&r3[0].equals(jzt.getId())) {
										if(jzt.getInfoType().equals("状态量")){
											if(jzt.getInterceptBit()!=null&&jzt.getInterceptBit().indexOf("bitIndex")!=-1) {
												String tmpinstruct=jzt.getInstruct();
					                            String tmpaddress=jzt.getAddress();
				                                for(int n=0;n<100;n++){
				                                	if((rk+n+1)>jztCollect.size()) {
				                                		break;
				                                	}
					                                JstZcTarget item = jztCollect.get(rk+n);
					                                if(item.getInstruct()!=tmpinstruct||item.getAddress()!=tmpaddress ){
					                                    break;
					                                };
		
					                           //     String aa = Integer.toBinaryString(Integer.parseInt(r3[1]));
					                           //     String a0=String.format("%16d", Integer.parseInt(aa)).replace(" ", "0");
					                                String str1=r3[1];
					                                if(str1.equals("true")) {
					                                	str1="1";
					                                }
					                                if(str1.equals("false")) {
					                                	str1="0";
					                                }
					                                
					                                String binaryStr = Integer.toBinaryString(Integer.parseInt(str1));
					                                while(binaryStr.length() < 16){
					                                    binaryStr = "0"+binaryStr;
					                                }
					                                
					                                String a1=item.getInterceptBit();
					                                String[] a2=a1.split(",");
					                                String[] a3=a2[0].split(":");
					                                int a4=Integer.parseInt(a3[1]);
					                                String a5 = binaryStr.substring(a4,a4+1);
					                                
													String bjz = item.getCtrlUp();
													if(bjz.indexOf("==")!=-1) {
														String[] bj = bjz.split("==");
														if(a5.equals(bj[1])) {
															alarmNo+=jzt.getId()+",";
															alarmValue+=jzt.getTargetName()+",";
														}
													}
													if(bjz.indexOf("!=")!=-1) {
														String[] bj = bjz.split("!=");
														if(!a5.equals(bj[1])) {
															alarmNo+=jzt.getId()+",";
															alarmValue+=jzt.getTargetName()+",";
														}
													}
				                                }
											}else {
												String bjz = jzt.getCtrlUp();
												if(bjz.indexOf("==")!=-1) {
													String[] bj = bjz.split("==");
													if(r3[1].equals(bj[1])) {
														alarmNo+=jzt.getId()+",";
														alarmValue+=jzt.getTargetName()+",";
													}
												}
												if(bjz.indexOf("!=")!=-1) {
													String[] bj = bjz.split("!=");
													if(!r3[1].equals(bj[1])) {
														alarmNo+=jzt.getId()+",";
														alarmValue+=jzt.getTargetName()+",";
													}
												}
											}
										}else {
											String[] mn = jzt.getCtrlDown().split(";");
											
											
											for(int rm=0;rm<mn.length;rm++) {
												String yinzi=jzt.getYinzi();
												if(mn[rm].indexOf("<")!=-1) {
													String a1 = mn[rm].replace("<", "").replace("=", "");
													float r4=0f;
													if(yinzi!=null) {
														r4=Integer.parseInt(r3[1])/Integer.parseInt(yinzi);
													}else {
														r4=Integer.parseInt(r3[1]);
													}
													if(r4<=Integer.parseInt(a1)) {
														alarmNo+=jzt.getId()+",";
														alarmValue+=jzt.getTargetName()+",";
													}
												}
												if(mn[rm].indexOf(">")!=-1) {
													String a1 = mn[rm].replace(">", "").replace("=", "");
											//		String yinzi=jzt.getYinzi();
													float r4=0f;
													String str=r3[1];
													if(str.contains(".")) {
														 int indexOf = str.indexOf(".");
														 str = str.substring(0, indexOf);
													}
													if(yinzi!=null) {
														r4=Integer.parseInt(str)/Integer.parseInt(yinzi);
													}else {
														r4=Integer.parseInt(str);
													}
													if(r4>=Integer.parseInt(a1)) {
														alarmNo+=jzt.getId()+",";
														alarmValue+=jzt.getTargetName()+",";
													}
												}
											}
											
										}
									}
								}
							}
						}
						if(alarmValue.length()>0) {
							List<JstZcAlarm> jzaList = jstZcAlarmService.queryJzaList("2");
							int dealflag=0; //初始状态
							for(int ai=0;ai<jzaList.size();ai++) {
								JstZcAlarm jza = jzaList.get(ai);
								if(jza.getDevNo().equals(devNo)&&jza.getTargetNo().equals(alarmNo)) {
									if(jza.getDealType()=="1") {  //已处理
										JstZcAlarm jstZcAlarm = new JstZcAlarm();
										jstZcAlarm.setDevNo(devNo);
										jstZcAlarm.setDevName(devName);
										jstZcAlarm.setCatNo(catNo);
										jstZcAlarm.setTargetNo(alarmNo);
										jstZcAlarm.setAlarmValue(alarmValue);
										jstZcAlarm.setSendTime(new Date());
										jstZcAlarm.setSendType("2");
										jstZcAlarmService.saveSys(jstZcAlarm);
										dealflag=2; //已处理
										break;
									}else {
										dealflag=1; //未处理
										jza.setSendTime(new Date());
										jstZcAlarmService.updateSys(jza);
									}
								}
							}
							if(dealflag==0 || dealflag==2) {
								JstZcAlarm jstZcAlarm = new JstZcAlarm();
								jstZcAlarm.setDevNo(devNo);
								jstZcAlarm.setDevName(devName);
								jstZcAlarm.setCatNo(catNo);
								jstZcAlarm.setTargetNo(alarmNo);
								jstZcAlarm.setAlarmValue(alarmValue);
								jstZcAlarm.setSendTime(new Date());
								jstZcAlarm.setSendType("2");
								jstZcAlarmService.saveSys(jstZcAlarm);
							}
						}else {
							List<JstZcAlarm> jzaList = jstZcAlarmService.queryJzaList("1");
						//	List<JstZcAlarm> jzaList = jstZcAlarmService.queryJzaList("0");
							for(int ai=0;ai<jzaList.size();ai++) {
								JstZcAlarm jza = jzaList.get(ai);
								if(jza.getDevNo().equals(devNo)) {
									jza.setSendType("-2");
									jstZcAlarmService.updateSys(jza);
						//			jstZcAlarmService.deleteSys(jza.getId());
								}
							}
						}
						Thread.sleep(200);
				    }
				} catch (ModbusInitException e) {
					e.printStackTrace();
				} catch (ModbusTransportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ErrorResponseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (Exception e)	            {
					e.printStackTrace();
			        Alarm alarm = new Alarm();
//			        alarm.setId("2");
			        alarm.setDevNo(devNo);
			        alarm.setTargetNo("mysql");
			        alarm.setAlarmValue("数据库保存失败");
			        alarm.setSendTime(new Date());
			        repository.insertAlarm(alarm);
	            } finally {
					master.destroy();
				}
			}
			
			if (type.equals("SNMP")) {
				version = jsonConInfo.getString("version");
				timeOut = jsonConInfo.getString("timeOut");
				community = jsonConInfo.getString("community");
				jztCollect.stream().sorted(Comparator.comparing(JstZcTarget::getInstruct));
				List<String> oidList = new ArrayList<String>();
				System.out.println(devNo+"::");
				for (int j = 0; j < jztCollect.size(); j++) {
					JstZcTarget jzt = jztCollect.get(j);
					String oidval = jzt.getInstruct();
					List snmpList = SnmpData.snmpGet(ipAddress, community, oidval,null);
					
					if(snmpList.size()>0) {
						for(int n=0;n<snmpList.size();n++) {
							resList.add(snmpList.get(n));
						}
					}else {
						break;
					}
					
				}
				
				
			}
			try {
				String resValue = org.apache.commons.lang.StringUtils.join(resList.toArray(),";");
		        Audit audit = new Audit();
		        audit.setDevNo(devNo);
		        audit.setAuditValue(resValue);
		        audit.setAuditTime(new Date());
		        repository.insertAudit(audit);
			} catch (Exception e) {
			      e.printStackTrace();
			}			
		}
		
		return Result.ok("巡检结束");
	}

	public boolean isNumeric(String str) {
		for (int i = 0; i < str.length(); i++) {
			System.out.println(str.charAt(i));
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}


	/**
	 * 编辑
	 *
	 * @param jstZcDev
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-编辑")
	@ApiOperation(value = "jst_zc_dev-编辑", notes = "jst_zc_dev-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody JstZcDev jstZcDev) {
		jstZcDevService.updateById(jstZcDev);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-通过id删除")
	@ApiOperation(value = "jst_zc_dev-通过id删除", notes = "jst_zc_dev-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		jstZcDevService.removeById(id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-批量删除")
	@ApiOperation(value = "jst_zc_dev-批量删除", notes = "jst_zc_dev-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.jstZcDevService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "jst_zc_dev-通过id查询")
	@ApiOperation(value = "jst_zc_dev-通过id查询", notes = "jst_zc_dev-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		JstZcDev jstZcDev = jstZcDevService.getById(id);
		if (jstZcDev == null) {
			return Result.error("未找到对应数据");
		}
		return Result.ok(jstZcDev);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param jstZcDev
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, JstZcDev jstZcDev) {
		return super.exportXls(request, jstZcDev, JstZcDev.class, "jst_zc_dev");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, JstZcDev.class);
	}

	class MyThread implements Runnable {
		private boolean allflag;
		private String catNo;
		
		public MyThread(boolean allflag, String catNo) {
			this.allflag = allflag;
			this.catNo = catNo;
		}

		@Override
		public void run() {
			try {
				long start, end;
				start = System.currentTimeMillis();
				threadWork(this.allflag,this.catNo);
				end = System.currentTimeMillis();
				System.out.println("开始时间:" + start + "; 结束时间:" + end + "; 用时:" + (end - start) + "(ms)");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
