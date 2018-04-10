package cn.i.search.core.api.index;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.i.search.core.common.BaseTestCase;
import cn.i.search.core.common.CommonUtil;
import cn.i.search.core.common.ConfigUtil;
import cn.i.search.core.common.ExcelUtil;

public class IndexServiceTest extends BaseTestCase {

	@Autowired
	private IndexService indexService;

	// 测试空索引创建方法--ok
	@Test
	public void createIndex() {
		indexService.createIndex("abc");
		assertEquals(true, indexService.isIndexExists("abc"));
	}

	// 测试带索引设置的索引创建方法--ok
	@Test
	public void createIndexWithSettings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("number_of_shards", 3);
		settings.put("number_of_replicas", 2);
		String settingsStr = CommonUtil.toJsonString(settings);
		indexService.createIndex("indexwithsettings", settingsStr);

		assertEquals(true, indexService.isIndexExists("indexwithsettings"));
	}

	// 测试带索引设置及mappings的索引创建方法--pending
	@Test
	public void createIndexWithMappings() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("number_of_shards", 3);
		settings.put("number_of_replicas", 2);
		String settingsStr = CommonUtil.toJsonString(settings);

		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("name", "text");
		mappings.put("key", "text");

		indexService.createIndex("mapindex", settingsStr);
		System.out.println(indexService.isIndexExists("mapindex"));
	}

	// 测试索引更新设置方法--提示Unassigned
	@Test
	public void updateIndex() {
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("number_of_replicas", 4);
		indexService.updateIndex("indexwithsettings", settings);

		assertTrue(indexService.isIndexExists("indexwithsettings"));
	}

	// 测试索引删除方法--ok
	@Test
	public void deleteIndex() {
		indexService.deleteIndex("indexwithsettings");
		assertEquals(false, indexService.isIndexExists("indexwithsettings"));
	}

	// 测试数据插入方法--ok
	@Test
	public void insertData() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("age", 22);
		data.put("name", "张三");
		data.put("job", "programmer");
		data.put("remark", "szse");

		String dataStr = CommonUtil.toJsonString(data);
		indexService.insertData("abc", dataStr);
	}

	// 测试带id的数据插入方法--ok
	@Test
	public void insertDataWithID() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("age", 24);
		data.put("name", "李四");
		data.put("job", "programmer");
		data.put("remark", "szse");

		String dataStr = CommonUtil.toJsonString(data);
		indexService.insertData("abc", "10012", dataStr);
	}

	// 测试数据删除方法--ok
	@Test
	public void deleteData() {
		indexService.deleteData("abc", "10012");
		System.out.println("数据删除成功！");
	}

	// 数据更新方法--ok
	@Test
	public void updateData() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("age", 21);
		data.put("name", "李四");
		data.put("job", "PM");
		data.put("remark", "szse");

		String dataStr = CommonUtil.toJsonString(data);
		indexService.updateData("abc", "10012", dataStr);
	}

	// 测试批量提交方法--ok
	@Test
	public void batchAddData() {
		List<Person> persons = new ArrayList<Person>();
		persons.add(new Person("1", "aaa"));
		persons.add(new Person("2", "bbb"));
		persons.add(new Person("3", "ccc"));

		indexService.batchAddData("abc", "id", persons);
	}

	// 完整测试
	@Test
	public void main() {
		// 测试前先将上次结果清空
		if (indexService.isIndexExists("abc")) {
			indexService.deleteIndex("abc");
		}
		createIndex();
		createIndexWithSettings();
		updateIndex();
		deleteIndex();
		insertData();
		insertDataWithID();
		updateData();
		deleteData();
		batchAddData();

		// 测试完成后，删除掉测试结果
		if (indexService.isIndexExists("abc")) {
			indexService.deleteIndex("abc");
		}
		System.out.println("测试完成");
	}

	// 测试数据导入--测试未通过，报Unknown filter type [dynamic_synonym] for [remote_synonym]异常
	@Test
	public void DataImport() {
		String path = "D:\\bigdata\\studing\\ElasticSearch\\test\\menu\\menu.xlsx";
		List<String[]> cells = ExcelUtil.getRows(path, 0);
		String indexName = "sms_menu";
		createMenuIndex(indexName);
		for(int i = 0;i<cells.size();i++) {
			String[] cell = cells.get(i);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("menu_name", cell[0]);
			data.put("menu_type", cell[1]);
			data.put("menu_item_id", cell[2]);
			data.put("menu_path", cell[4]);
			data.put("usr_id", cell[5]);
			data.put("visit_times", 0);
			String[] labels = cell[3].split(",");
			if(labels.length > 0) {
				data.put("labels", labels);
			}
			String[] menu_info = cell[7].split("、");
			if(menu_info.length > 0) {
				data.put("menu_info", menu_info);
			}
			String dataStr = CommonUtil.toJsonString(data);
			indexService.insertData(indexName, dataStr);
		}
		System.out.println("导入成功！");
	}

	// 创建菜单索引项
	public void createMenuIndex(String indexName) {
		if (!indexService.isIndexExists(indexName)) {
			String url = IndexServiceTest.class.getClassLoader().getResource("mappings/menu.search.xml").getFile();
			String settings = ConfigUtil.getStringForXmlFile(url, ConfigUtil.SETTINGS);
			String mappings = ConfigUtil.getStringForXmlFile(url, ConfigUtil.MAPPINGS);
			indexService.createIndex(indexName, settings, mappings);
		}
	}

}
